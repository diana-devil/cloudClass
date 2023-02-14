package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.IOrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName OrderController
 * @Date 2023/2/14 13:49
 * @Author diane
 * @Description 订单接口
 * @Version 1.0
 */
@RestController
@Slf4j
@Api(value = "订单支付接口", tags = "订单支付接口")
public class OrderController {

    @Resource
    private IOrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.exce("用户未登录！");
        }
        String userId = user.getId();
        return orderService.generatePayCode(addOrderDto, userId);
    }



    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo,
                       HttpServletResponse httpResponse) throws ServletException, IOException, AlipayApiException {
        // 根据支付记录号，得到支付记录信息
        XcPayRecord payRecord = orderService.getPayRecordByPayno(payNo);

        log.info("我被调用了！");
        AlipayClient client = new DefaultAlipayClient(AlipayConfig.URL, AlipayConfig.APPID, AlipayConfig.RSA_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        // 支付完成后，跳转到页面 -- 不推荐，但是在没有备案域名和内网穿透的的情况下，也可以用
        alipayRequest.setReturnUrl("http://192.168.159.1/api/orders/returnres");
        // alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        // -- 支付宝通知订单服务的地址必须为外网域名且备案通过可以正常访问 -- 调试的时候需要使用内网穿透技术
        // alipayRequest.setNotifyUrl("http://tjxt-user-t.itheima.net/xuecheng/orders/receivenotify");//在公共参数中设置回跳和通知地址

        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\""+payRecord.getPayNo()+"\"," +
                " \"total_amount\":\""+payRecord.getTotalPrice()+"\"," +
                " \"subject\":\""+payRecord.getOrderName()+"\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数


        String form = client.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();

    }



    @ApiOperation("接收支付结果通知-- 支付宝主动通知")
    // 这个方法 没啥用， 因为我们没有备案域名
    @PostMapping("/receivenotify")
    public void receivenotify(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        //验签
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");
        //验证成功
        if (verify_result) {
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            //appid
            String app_id = new String(request.getParameter("app_id").getBytes("ISO-8859-1"),"UTF-8");
            //total_amount
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            //交易成功处理
            if (trade_status.equals("TRADE_SUCCESS")) {

                // 封装 dto信息
                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setApp_id(app_id);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTotal_amount(total_amount);

                //校验支付信息，更新支付状态
                orderService.saveAliPayStatus(payStatusDto);

            }
        }

    }




    //http://192.168.159.1/api/orders/returnres?charset=UTF-8&out_trade_no=1625412647767183360&method=alipay.trade.wap.pay.return&total_amount=10.00&sign=Z9nru0%2BfrhlVX9FFqFPyldT%2BaRupRGo2pjE22c%2BtIG%2BdXuhUSjFbkFTr0EzYBEmpnX71Azag9VRYPS9K7cXkVE%2BvjWSZRCdRhHuqFvY8ZOdfV%2BKZrdKwriks8ujfOkGUKnBOrwlJvKOJqiUT3vFSc%2FcJIOV8VdvFOjdcHGwPnho19Wpo%2BIHf3%2FB%2FOaFhHT%2BS%2B2ifuHTkQjtYFWYoRZFMpIfGHU1PL1EbzBWmtOelyIKN7Ssh2W%2BlbumdOqxsPOH5ZeLsfvDnU1V8MgPVLaOw8WtVA%2BCno%2BuUk%2Btu8W8g6roIdjEfhErGxLOirkJ6%2F2RzGPFqRliQIgRc4lKNgX3ICg%3D%3D&trade_no=2023021422001402400501741930&auth_app_id=2021000122612975&version=1.0&app_id=2021000122612975&sign_type=RSA2&seller_id=2088621995202750&timestamp=2023-02-14+16%3A37%3A54
    // public void receiveres(@RequestParam String app_id, @RequestParam String sign_type, @RequestParam String sign,
    //                        @RequestParam String out_trade_no, @RequestParam String trade_no,
    //                        @RequestParam String total_amount) throws UnsupportedEncodingException, AlipayApiException {
    @ApiOperation("接收支付结果通知-- 网页跳转形式")
    @RequestMapping("/returnres")
    public void receiveres(HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {

        //获取支付宝GET过来反馈信息
        // key 获取网页的一大堆？链接的参数  使用request对象逐个获取即可
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }


        //验签
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, params.get("charset"), params.get("sign_type"));
        //验证成功
        if (verify_result) {
            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //交易状态 -- 这种格式 没有 这个字段 -- 默认是支付成功了才会跳转到这
            // String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            //appid
            String app_id = new String(request.getParameter("app_id").getBytes("ISO-8859-1"),"UTF-8");
            //total_amount
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");

            //交易成功处理
            // 封装 dto信息
            PayStatusDto payStatusDto = new PayStatusDto();
            payStatusDto.setOut_trade_no(out_trade_no);
            payStatusDto.setApp_id(app_id);
            payStatusDto.setTrade_no(trade_no);
            payStatusDto.setTotal_amount(total_amount);
            // 原参数没有这个字段，这里手动添加，可以共用方法saveAliPayStatus
            payStatusDto.setTrade_status("TRADE_SUCCESS");

            //校验支付信息，更新支付状态
            orderService.saveAliPayStatus(payStatusDto);

        }

    }





}
