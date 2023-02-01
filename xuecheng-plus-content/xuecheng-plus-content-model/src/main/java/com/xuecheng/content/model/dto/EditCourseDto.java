package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @ClassName EditCourseDto
 * @Date 2023/2/1 20:53
 * @Author diane
 * @Description 修改课程信息 dto  比 新增课程信息dto  多了课程id
 *      因为新增课程，id是自动生成的；而修改课程，需要根据课程id进行修改
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    @NotNull(message = "未确定要修改的课程")
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
