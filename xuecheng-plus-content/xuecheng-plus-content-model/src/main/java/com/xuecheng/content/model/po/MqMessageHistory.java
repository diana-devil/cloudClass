package com.xuecheng.content.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName mq_message_history
 */
@TableName(value ="mq_message_history")
@Data
public class MqMessageHistory implements Serializable {
    /**
     * 消息id
     */
    @TableId
    private Long id;

    /**
     * 消息类型代码
     */
    private String messageType;

    /**
     * 关联业务信息
     */
    private String businessKey1;

    /**
     * 关联业务信息
     */
    private String businessKey2;

    /**
     * 关联业务信息
     */
    private String businessKey3;

    /**
     * 通知次数
     */
    private Object executeNum;

    /**
     * 处理状态，0:初始，1:成功，2:失败
     */
    private Integer state;

    /**
     * 回复失败时间
     */
    private Date returnfailureDate;

    /**
     * 回复成功时间
     */
    private Date returnsuccessDate;

    /**
     * 回复失败内容
     */
    private String returnfailureMsg;

    /**
     * 最近通知时间
     */
    private Date executeDate;

    /**
     * 
     */
    private String stageState1;

    /**
     * 
     */
    private String stageState2;

    /**
     * 
     */
    private String stageState3;

    /**
     * 
     */
    private String stageState4;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}