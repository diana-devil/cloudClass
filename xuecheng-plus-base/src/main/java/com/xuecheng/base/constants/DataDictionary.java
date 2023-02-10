package com.xuecheng.base.constants;

/**
 * @ClassName SystemConstants
 * @Date 2023/2/9 16:56
 * @Author diane
 * @Description 数据字典
 * @Version 1.0
 */
public class DataDictionary {

    // 审核状态
    public static final String  AUDIT_UNCOMMITTED= "202002";
    public static final String  AUDIT_COMMITTEDED= "202003";
    public static final String  AUDIT_NOT_PASS= "202001";
    public static final String  AUDIT_PASS= "202004";

    // 发布状态
    public static final String  PUBLISH_NOT= "203001";
    public static final String  PUBLISH_YES= "203002";
    public static final String  PUBLISH_DOWN= "203003";


    // 课程收费状态
    public static final String CHARGE_FREE = "201000";
    public static final String CHARGE_NOT_FREE = "201001";

    // 资源类型
    public static final String RESOURCE_IMG = "001001";
    public static final String RESOURCE_VIDEO = "001002";
    public static final String RESOURCE_OTHER = "001003";

    // 课程等级
    public static final String COURSE_GRADE_PRIMARY = "204001";
    public static final String COURSE_GRADE_MIDDLE = "204002";
    public static final String COURSE_GRADE_ADVANCED = "204003";

    // 课程模式状态
    public static final String COURSE_PATTERN_LIVE = "200003";
    public static final String COURSE_PATTERN_RECOVERED = "200002";

    // 订单交易类型状态
    public static final String ORDER_PAY = "600002";
    public static final String ORDER_NOT_PAY = "600001";
    public static final String ORDER_CLOSE = "600003";
    public static final String ORDER_REFUND= "600004";
    public static final String ORDER_FINISH = "600005";

    // 课程作业记录审批状态
    public static final String COURSE_WORK_UNCOMMITTEDED = "306001";
    public static final String COURSE_WORK_CHECK = "306003";
    public static final String COURSE_WORK_UNCHECK = "306002";


    // 消息通知状态
    public static final String MESSAGE_NO = "003001";
    public static final String MESSAGE_SUCCESS = "003002";


    // 支付记录交易状态
    public static final String PAYMENT_PAY = "601002";
    public static final String PAYMENT_NOT_PAY = "601001";
    public static final String PAYMENT_REFUND = "601003";


    // 业务订单类型
    public static final String ORDER_TYPE_COURSE = "60201";
    public static final String ORDER_TYPE_DATA = "60202";


    // 第三方支付渠道编号
    public static final String PAYMENT_TYPE_WEIXIN = "603001";
    public static final String PAYMENT_TYPE_ZFB = "603002";


    // 选课状态
    public static final String COURSE_SELECTION_SUCCESS = "701001";
    public static final String COURSE_SELECTION_NOT_PAY = "701002";


    // 选课学习资格
    public static final String STUDY_NORMAL = "702001";
    public static final String STUDY_NOCOURSE = "702002";
    public static final String STUDY_OVERDUE = "702003";

}