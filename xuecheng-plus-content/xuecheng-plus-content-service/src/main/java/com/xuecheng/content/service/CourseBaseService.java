package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;


/**
* @author 凉冰
* @description 针对表【course_base(课程基本信息)】的数据库操作Service
* @createDate 2023-01-31 09:55:38
*/
public interface CourseBaseService extends IService<CourseBase> {

    /**
     * 课程查询
     * @param pageParams 分页参数
     * @param queryCourseParams 查询条件
     * @return 分页数据
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams);


    /**
     * 新增课程
     * @param addCourseDto  新增课程信息
     * @param companyId 培训机构id
     * @return 课程信息 = 课程基本信息 + 营销信息
     */
    CourseBaseInfoDto createCourseBase(AddCourseDto addCourseDto, Long companyId);

    /**
     * 课程回显
     * @param courseId 课程id
     * @return  课程信息 = 课程基本信息 + 营销信息
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 课程修改
     * @param editCourseDto 修改课程信息 dto
     * @return 课程信息
     */
    CourseBaseInfoDto modifyCourseBase(EditCourseDto editCourseDto, Long companyId);
}
