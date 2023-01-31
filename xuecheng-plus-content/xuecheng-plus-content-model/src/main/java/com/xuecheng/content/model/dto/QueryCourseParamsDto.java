package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * @ClassName QueryCourseParamDto
 * @Date 2023/1/30 21:10
 * @Author diane
 * @Description 课程查询参数Dto
 * @Version 1.0
 */
@Data
public class QueryCourseParamsDto {
    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

    public QueryCourseParamsDto(){}

    public QueryCourseParamsDto(String auditStatus, String courseName, String publishStatus){
        this.auditStatus = auditStatus;
        this.courseName = courseName;
        this.publishStatus = publishStatus;
    }



}
