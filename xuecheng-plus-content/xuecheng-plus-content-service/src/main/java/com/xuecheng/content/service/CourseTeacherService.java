package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;


/**
* @author 凉冰
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Service
* @createDate 2023-01-31 09:55:38
*/
public interface CourseTeacherService extends IService<CourseTeacher> {

    /**
     * 获取 课程对应教师信息
     * @param id 课程id
     * @return 教师信息
     */
    List<CourseTeacher> getCourseTeacher(Long id);

    /**
     *  新增/修改 师资信息
     * @param courseTeacher  师资信息
     */
    void saveCourseTeacher(CourseTeacher courseTeacher);

    /**
     *  删除师资信息
     * @param courseId 课程id
     * @param id 师资id
     */
    void removeCourseTeacher(Long courseId, Long id);
}
