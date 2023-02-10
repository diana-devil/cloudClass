package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import lombok.Data;

import java.util.List;

/**
 * @ClassName CoursePreviewDto
 * @Date 2023/2/9 14:53
 * @Author diane
 * @Description 课程预览 数据模型
 * @Version 1.0
 */
@Data
public class CoursePreviewDto {
    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息
    List<CourseTeacher> courseTeachers;

}
