package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.xuecheng.base.constants.DataDictionary.ORDER_TYPE_COURSE;
import static com.xuecheng.base.constants.SystemConstants.MESSAGE_TYPE_ORDER;

/**
 * @author Mr.M
 * @version 1.0
 * @description 接收支付结果通知service
 * @date 2022/10/5 5:06
 */
@Slf4j
@Service
public class ReceivePayNotifyService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MyCourseTablesService myCourseTablesService;


    //接收支付结果通知
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = PayNotifyConfig.PAYNOTIFY_QUEUE),
//            exchange = @Exchange(value = PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, type = ExchangeTypes.FANOUT)
//
//    ))
    // 监听支付结果通知队列
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(String message) {
        //获取消息
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        log.info("学习中心服务接收支付结果:{}", mqMessage);

        // 消息类型判断
        String messageType = mqMessage.getMessageType();  // 消息类型
        String chooseCourseId = mqMessage.getBusinessKey1();  // 选课id
        String orderType = mqMessage.getBusinessKey2(); // 业务订单类型
        if (!(MESSAGE_TYPE_ORDER.equals(messageType) && ORDER_TYPE_COURSE.equals(orderType))) {
            log.info("消息类型不符合 学习中心服务的监听");
            return;
        }

        //更新 选课状态为 选课成功，并将课程插入 我的课程表
        boolean b = myCourseTablesService.saveChooseCourseStauts(chooseCourseId);
        if (b) {
            log.info("更新选课状态和我的课程表成功！向交换机投递信息:{}", mqMessage);
            //向订单服务回复
            send(mqMessage);
        }

    }

    /**
     * @description 向 消息处理成功反馈消息队列 回复消息
     * @param message  回复消息
     * @return void
     * @author Mr.M
     * @date 2022/9/20 9:43
     */
    public void send(MqMessage message){
        //转json
        String msg = JSON.toJSONString(message);
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_REPLY_QUEUE, msg);
        log.info("学习中心服务向订单服务回复消息:{}",message);
    }



}
