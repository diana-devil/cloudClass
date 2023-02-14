package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.xuecheng.base.constants.DataDictionary.*;

/**
 * @ClassName OrderServiceImpl
 * @Date 2023/2/14 13:55
 * @Author diane
 * @Description 订单 service 实现类
 * @Version 1.0
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<XcOrdersMapper, XcOrders> implements IOrderService {


    @Resource
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Resource
    private XcPayRecordMapper payRecordMapper;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;


    /**
     * 生成支付二维码
     * @param addOrderDto 支付信息
     * @param userId 用户id
     * @return
     */
    @Override
    public PayRecordDto generatePayCode(AddOrderDto addOrderDto, String userId) {
        // 创建商品订单
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);
        // 添加支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        // 生成支付二维码
        String qrCode = null;
        try {
            //url要可以被模拟器访问到，url为下单接口(稍后定义)
            // /api 是代理到网关了
            String url = "http://192.168.159.1/api/orders/requestpay?payNo="+payRecord.getPayNo();
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.exce("生成二维码出错");
        }
        // 创建 返回数据， 加了一个二维码
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    /**
     * 查询支付交易记录
     * @param payNo  交易记录号
     * @return
     */
    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        if (xcPayRecord == null) {
            XueChengPlusException.exce("交易记录不存在！");
        }
        return xcPayRecord;
    }

    /**
     * 支付宝支付
     * 校验支付信息，更新支付状态
     * @param payStatusDto 支付状态dto
     */
    @Override
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        // 支付宝 返回的 支付结果
        String trade_status = payStatusDto.getTrade_status();
        // 只有支付成功了，才去进行 数据库操作
        if (!trade_status.equals("TRADE_SUCCESS")) {
            log.info("第三方支付返回结果不为 支付成功,订单交易未完成！");
            return;
        }

        //支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        //查询支付流水
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            log.info("收到支付结果通知，但是没有查询到支付记录！");
            return;
        }

        //支付金额变为分
        float totalPriceDb = payRecord.getTotalPrice() * 100;//数据库查询的总金额
        float total_amount = Float.parseFloat(payStatusDto.getTotal_amount()) * 100; //第三方支付给的总金额
        //校验数据是否一致
        if (!(payStatusDto.getApp_id().equals(APP_ID)
                && (int) totalPriceDb == (int) total_amount)) {
            log.info("收到支付结果通知，但是数据库信息与商家信息不一致！");
            return;
        }

        // 检测支付状态
        String status = payRecord.getStatus();
        if (PAYMENT_PAY.equals(status)) {
            log.info("收到支付结果通知，支付记录已经为 支付成功,无需再次更新！");
            return;
        }

        //未支付时进行处理 -- 更新支付记录表
        log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo, trade_status);
        // 组装更新信息
        XcPayRecord payRecord_u = new XcPayRecord();
        payRecord_u.setStatus(PAYMENT_PAY);//支付成功
        payRecord_u.setOutPayChannel(PAYMENT_TYPE_ZFB);// 支付宝支付
        payRecord_u.setOutPayNo(payStatusDto.getTrade_no());//支付宝交易号
        payRecord_u.setPaySuccessTime(LocalDateTime.now());//通知时间
        // 根据 payNo 字段 进行更新
        int update1 = payRecordMapper.update(payRecord_u, new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));

        if (update1 > 0) {
            log.info("收到支付通知，更新支付交易状态成功.付交易流水号:{},支付结果:{}", payNo, trade_status);
        } else {
            log.error("收到支付通知，更新支付交易状态失败.支付交易流水号:{},支付结果:{}", payNo, trade_status);
        }

        //关联的订单号  --  更新订单表的 status状态
        Long orderId = payRecord.getOrderId();
        XcOrders orders = getById(orderId);
        if (orders == null) {
            log.error("收到支付通知，根据交易记录找不到订单,交易记录号:{},订单号:{}",payRecord.getPayNo(),orderId);
            // XueChengPlusException.exce("订单记录为空！");
            return;
        }
        // 更新订单表的 status状态
        XcOrders order_u = new XcOrders();
        order_u.setStatus(ORDER_PAY);
        // 根据 id字段更新，只更新status这一个字段
        boolean bool = update(order_u, new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getId, orderId));
        if (bool) {
            log.info("收到支付通知，更新订单状态成功.付交易流水号:{},支付结果:{},订单号:{},状态:{}", payNo, trade_status, orderId, ORDER_PAY);
        } else {
            log.error("收到支付通知，更新订单状态失败.支付交易流水号:{},支付结果:{},订单号:{},状态:{}", payNo, trade_status, orderId, ORDER_PAY);
        }

    }



    //根据业务id查询订单
    public XcOrders getOrderByBusinessId(String businessId) {
        return getOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
    }

    /**
     * 创建商品订单，并将订单信息 插入 xc_orders;xc_orders_goods
     * @param userId 用户id
     * @param addOrderDto 订单信息
     * @return XcOrders 对象
     */
    @Transactional
    public XcOrders saveXcOrders(String userId,AddOrderDto addOrderDto){
        // 1. 幂等性处理
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if(order!=null){
            return order;
        }


        // 2. 将订单信息 存入 xc_orders表
        order = new XcOrders();
        // copy基本属性
        BeanUtils.copyProperties(addOrderDto, order);
        // 补全order属性
        // id 雪花算法生成
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(ORDER_NOT_PAY);//未支付
        order.setUserId(userId);
        save(order);

        // 3. 将订单信息存入 xc_orders_goods中
        // 从订单明细的json串中 解析 具体的商品信息
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods->{
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods,xcOrdersGoods);
            // 订单详情表与订单表的关联字段
            xcOrdersGoods.setOrderId(orderId);//订单号
            xcOrdersGoodsMapper.insert(xcOrdersGoods);
        });

        // 返回 订单对象
        return order;
    }


    /**
     * 创建支付记录 ， 并存入数据库
     *      不需要做幂等性判断；
     *      用户每次发起支付，都创建并存入一条支付记录
     *      这么做的好处 是方便和第三方，（支付宝，微信），对接
     * @param orders 订单信息
     * @return 支付记录信息
     */
    public XcPayRecord createPayRecord(XcOrders orders){

        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号-雪花算法
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        // 商品订单号  支付记录表与订单表的关联字段
        payRecord.setOrderId(orders.getId());//商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus(PAYMENT_NOT_PAY);//未支付
        payRecord.setUserId(orders.getUserId());
        payRecordMapper.insert(payRecord);
        return payRecord;

    }


    /**
     * 主动查询支付结果；并且校验支付信息，更新支付状态
     */
    @Override
    public void queryAndSave() {


    }
}
