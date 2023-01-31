package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
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
     * @return
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams);

}
