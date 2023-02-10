package com.xuecheng.content.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

import static com.xuecheng.base.constants.SystemConstants.MESSAGE_TYPE_COURSE;


/**
 * @ClassName CoursePublishTask
 * @Date 2023/2/9 21:18
 * @Author diane
 * @Description 课程发布任务
 * @Version 1.0
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {


    @Resource
    private CoursePublishService coursePublishService;


    /**
     * 课程发布执行任务(逻辑)
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        log.info("开始执行课程发布任务，课程id-{}", mqMessage.getBusinessKey1());
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //课程静态化-freemarker-minio-mysql
        generateAndUploadCourseHtml(mqMessage,courseId);

        //课程缓存-redis
        saveCourseCache(mqMessage,courseId);

        //课程索引-es
        saveCourseIndex(mqMessage,courseId);


        return true;
    }


    /**
     * 创建 课程缓存
     * @param mqMessage 消息
     * @param courseId 课程id
     */
    private void saveCourseCache(MqMessage mqMessage, long courseId) {

    }




    /**
     * 创建课程索引
     * @param mqMessage 消息
     * @param courseId 课程id
     */
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        // 判断该任务是否完成，保证幂等性
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo > 0) {
            log.info("课程索引已处理直接返回，课程id:{}",courseId);
            return;
        }
        // 创建课程索引
        Boolean res = coursePublishService.saveCourseIndex(courseId);

        //保存第二阶段状态  status 置1
        if (res) {
            mqMessageService.completedStageTwo(id);
        }
    }


    /**
     * 将课程信息静态化，同时将课程信息上传到minio
     * @param mqMessage 消息
     * @param courseId 课程id
     */
    private void generateAndUploadCourseHtml(MqMessage mqMessage, long courseId) {
        // 判断该任务是否完成，保证幂等性
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0) {
            log.info("课程静态化已处理直接返回，课程id:{}",courseId);
            return;
        }

        // 生成静态文件 html
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.exce("生成静态文件异常");
        }

        // 将 html 上传至 minio
        coursePublishService.uploadCourseHtml(courseId, file);

        //保存第一阶段状态  status 置1
        mqMessageService.completedStageOne(id);
    }


    /**
     * 任务调度函数
     * @throws Exception
     */
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        // 扫描消息表多线程执行任务
        process(shardIndex, shardTotal, MESSAGE_TYPE_COURSE, 5, 60);
    }

}
