package com.xuecheng.content.api;

import com.xuecheng.base.common.ValidationGroups;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.xuecheng.content.utils.SecurityUtil.getUser;

/**
 * @ClassName CourseBaseInfoController
 * @Date 2023/1/30 21:29
 * @Author diane
 * @Description 课程信息编辑接口，带swagger文档
 * @Version 1.0
 */
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
@Slf4j
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseService courseBaseService;

    @ApiOperation("课程查询")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('course_find_list')")//拥有课程列表权限方可访问
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParams){
        // 获取用户身份
        SecurityUtil.XcUser user = getUser();
        if (user == null) {
            XueChengPlusException.exce("用户不存在！");
        }
        // 获取用户所属公司id
        Long companyId = user.getCompanyId();

        // 通过 公司id 实现细粒度授权
        return courseBaseService.queryCourseBaseList(pageParams, queryCourseParams, companyId);
    }


    @ApiOperation("新增课程")
    @PostMapping
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {

        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;

        return courseBaseService.createCourseBase(addCourseDto, companyId);
    }

//    @ApiOperation("新增课程接口")
//    @PostMapping("/course2")
//    public CourseBaseInfoDto createCourseBase2(@RequestBody @Validated(ValidationGroups.Update.class) AddCourseDto addCourseDto) {
//
//        //机构id，由于认证系统没有上线暂时硬编码
//        Long companyId = 1L;
//
//        return courseBaseService.createCourseBase(addCourseDto, companyId);
//
//    }

    @ApiOperation("课程回显")
    @GetMapping("/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
        // SecurityUtil.XcUser user = getUser();
        // String username = user.getUsername();
        // log.info(username);

        return courseBaseService.getCourseBaseInfo(courseId);
    }


    @ApiOperation("课程修改")
    @PutMapping
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {

        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;

        return courseBaseService.modifyCourseBase(editCourseDto, companyId);
    }




}