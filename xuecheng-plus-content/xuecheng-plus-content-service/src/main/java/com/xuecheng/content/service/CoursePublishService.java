package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;


/**
* @author 凉冰
* @description 针对表【course_publish(课程发布)】的数据库操作Service
* @createDate 2023-01-31 09:55:38
*/
public interface CoursePublishService extends IService<CoursePublish> {

    /**
     *  根据课程id 查询 所有课程相关信息
     * @param courseId 课程id
     * @return 预览信息
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     * @param companyId 公司id
     * @param courseId 课程id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     * @param companyId 公司id
     * @param courseId 课程id
     */
    void coursepublish(Long companyId, Long courseId);


    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    public File generateCourseHtml(Long courseId);


    /**
     * @description 上传课程静态化页面
     * @param courseId 课程id
     * @param file  静态化文件
     * @return void
     */
    public void  uploadCourseHtml(Long courseId,File file);

    /**
     * 创建课程索引
     * @param courseId 课程id
     */
    Boolean saveCourseIndex(long courseId);

    /**
     * 查询课程发布信息
     * @param courseId 课程id
     * @return 课程发布表信息
     */
    CoursePublish getCoursePublish(Long courseId);

    /**
     * 利用redis 进行缓存优化-查询课程发布信息
     * @param courseId 课程id
     * @return 课程发布信息
     */
    CoursePublish getCoursePublishCache(Long courseId);

    /**
     * 加jvm本地锁，来防止各种问题
     * @param courseId 课程id
     * @return 课程发布信息
     */
    public CoursePublish getCoursePublishCache2(Long courseId);

    /**
     * 使用redisson 加分布式锁
     * @param courseId 课程id
     * @return 课程发布信息
     */
    public CoursePublish getCoursePublishCache3(Long courseId);
}
