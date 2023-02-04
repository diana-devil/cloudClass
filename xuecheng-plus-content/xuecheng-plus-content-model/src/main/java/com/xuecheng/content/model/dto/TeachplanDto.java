package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @ClassName TeachplanDto
 * @Date 2023/2/4 19:59
 * @Author diane
 * @Description 课程计划树型结构dto
 * @Version 1.0
 */
@ApiModel(value="TeachplanDto", description="课程计划树形信息")
@Data
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    //子结点
    List teachPlanTreeNodes;

}
