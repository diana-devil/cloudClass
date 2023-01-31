package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 凉冰
* @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
* @createDate 2023-01-31 09:55:38
*/
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
    implements CourseBaseService {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {

        // 分页参数
        IPage<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 课程名称模糊查询
        String courseName = queryCourseParams.getCourseName();
        //第一个条件是判空
        queryWrapper.like(StringUtils.isNotBlank(courseName),CourseBase::getName, courseName);
        // 课程审核状态
        String auditStatus = queryCourseParams.getAuditStatus();
        queryWrapper.eq(StringUtils.isNotBlank(auditStatus),CourseBase::getAuditStatus, auditStatus);
        // 课程发布状态
        String publishStatus = queryCourseParams.getPublishStatus();
        queryWrapper.eq(StringUtils.isNotBlank(publishStatus),CourseBase::getStatus, publishStatus);


        //E page 分页参数, @Param("ew") Wrapper<T> queryWrapper 查询条件
        IPage<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);


        // 拼装返回结果
        PageResult<CourseBase> result = new PageResult<CourseBase>();
        //总记录数
        result.setCounts(pageResult.getTotal());
        // 数据列表
        result.setItems(pageResult.getRecords());
        result.setPage(pageParams.getPageNo());
        result.setPageSize(pageParams.getPageSize());


        return result;
    }
}




