package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CoursePublishController
 * @Date 2023/2/9 11:26
 * @Author diane
 * @Description 课程预览，发布 接口
 * @Version 1.0
 */
@Controller
@Api(value = "课程预览，发布 接口",tags = "课程预览，发布 接口")
public class CoursePublishController {

    @Resource
    private CoursePublishService coursePublishService;


    /**
     * 课程预览
     * @param courseId 课程id
     * @return  ModelAndView  页面
     */
    @ApiOperation("课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        // 获取模型信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }


    /**
     * 提交审核
     * @param courseId 课程id
     */
    @ApiOperation("提交审核")
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }


    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.coursepublish(companyId, courseId);
    }


    @ApiOperation("查询课程发布信息-- 微服务中调用")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    // 前面加上 /r/ 表示不需要认证授权，供内部微服务调用
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }


    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId) {
        // CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        // 利用redis优化
        // CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        // CoursePublish coursePublish = coursePublishService.getCoursePublishCache2(courseId);
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache3(courseId);

        if (coursePublish == null) {
            return new CoursePreviewDto();
        }

        // 封装基本信息 和 营销信息
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBaseInfoDto);

        // 封装教学计划信息-json格式
        String teachplan = coursePublish.getTeachplan();
        List<TeachplanDto> teachplanDtos = JSON.parseArray(teachplan, TeachplanDto.class);

        // 封装教师信息-json格式
        String teachers = coursePublish.getTeachers();
        List<CourseTeacher> courseTeachers = JSON.parseArray(teachers, CourseTeacher.class);

        // 封装 返回信息
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachplanDtos);
        coursePreviewDto.setCourseTeachers(courseTeachers);

        return coursePreviewDto;

    }








    }
