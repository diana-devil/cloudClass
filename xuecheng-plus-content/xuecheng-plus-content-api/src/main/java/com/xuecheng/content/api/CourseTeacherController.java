package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CourseTeacher
 * @Date 2023/2/6 11:19
 * @Author diane
 * @Description TODO
 * @Version 1.0
 */
@RequestMapping("/courseTeacher")
@RestController
@Slf4j
@Api(value = "师资管理编辑接口", tags = "师资管理编辑接口")
public class CourseTeacherController {
    @Resource
    private CourseTeacherService courseTeacherService;


    @ApiOperation("查询师资信息")
    @GetMapping("/list/{id}")
    public List<CourseTeacher> getCourseTeacher(@PathVariable Long id) {
        return courseTeacherService.getCourseTeacher(id);
    }

    @ApiOperation("修改/新增师资信息")
    @PostMapping
    public void saveCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        courseTeacherService.saveCourseTeacher(courseTeacher);
    }

    @ApiOperation("删除师资信息")
    @DeleteMapping("/course/{courseId}/{id}")
    public void removeCourseTeacher(@PathVariable Long courseId, @PathVariable Long id) {
        courseTeacherService.removeCourseTeacher(courseId, id);
    }
}
