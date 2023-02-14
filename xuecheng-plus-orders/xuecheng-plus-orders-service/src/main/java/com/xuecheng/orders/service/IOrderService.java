package com.xuecheng.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * @ClassName IOrderService
 * @Date 2023/2/14 13:53
 * @Author diane
 * @Description 订单service
 * @Version 1.0
 */
public interface IOrderService extends IService<XcOrders> {
    /**
     * 生成支付二维码
     * @param addOrderDto 支付信息
     * @param userId 用户id
     * @return
     */
    PayRecordDto generatePayCode(AddOrderDto addOrderDto, String userId);


    /**
     * @description 查询支付交易记录
     * @param payNo  交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @author Mr.M
     * @date 2022/10/20 23:38
     */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 校验支付信息，更新支付状态
     * @param payStatusDto 支付状态dto
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 主动查询支付结果；并且校验支付信息，更新支付状态
     */
    void queryAndSave();
}
