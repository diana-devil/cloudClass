package com.xuecheng.orders.jobhandler;

import com.alibaba.fastjson.JSON;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.xuecheng.base.constants.SystemConstants.MESSAGE_TYPE_ORDER;
import static com.xuecheng.orders.config.PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT;

/**
 * @ClassName PayNotifyTask
 * @Date 2023/2/14 19:42
 * @Author diane
 * @Description 支付通知任务
 *      由 xxl-job 监控 mq_message表 ,只要有对应的信息,就向交换机发送消息
 * @Version 1.0
 */
@Component
@Slf4j
public class PayNotifyTask extends MessageProcessAbstract {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MqMessageService mqMessageService;

    /**
     * 任务调度函数 -- 任务调度入口
     * @throws Exception
     */
    @XxlJob("NotifyPayResultJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        // 扫描消息表多线程执行任务
        process(shardIndex, shardTotal, MESSAGE_TYPE_ORDER, 5, 60);
    }



    /**
     * 执行任务的具体方法
     * @param mqMessage 执行任务内容
     * @return bool类型
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        log.info("开始进行支付结果通知:{}",mqMessage.toString());
        // 属于生成者, 将消息投递给交换机；
        send(mqMessage);
        //由于消息表的记录需要等到订单服务收到回复后才能删除，这里返回false不让消息sdk自动删除
        return false;
    }


    /**
     * 交换机将 消息 进行广播
     * @param message 消息对象
     */
    private void send(MqMessage message){
        // 将消息转换成json串
        String msg = JSON.toJSONString(message);
        // 使用的是 Fanout-广播模式  不使用 routingKey
        rabbitTemplate.convertAndSend(PAYNOTIFY_EXCHANGE_FANOUT, "", msg);
        log.info("支付结果通知完成:{}",msg);
    }



    /**
     * 监听 消息反馈队列；从中获取 回复
     * @param message 回复消息
     */
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_REPLY_QUEUE)
    public void receive(String message) {
        //获取消息
        MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
        log.info("接收支付结果回复:{}", mqMessage);

        //完成支付通知
        mqMessageService.completed(mqMessage.getId());

    }

}
