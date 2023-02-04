package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * @ClassName SaveTeachplanDto
 * @Date 2023/2/4 21:01
 * @Author diane
 * @Description 课程计划保存信息 dto
 *      根据是否包含  课程计划id  来区分 新增课程计划，还是修改课程计划
 * @Version 1.0
 */
@ApiModel(value="SaveTeachplanDto", description="课程计划保存信息 dto")
@Data
@ToString
public class SaveTeachplanDto {

  /***
   * 教学计划id
   */
  private Long id;

  /**
   * 课程计划名称
   */
  private String pname;

  /**
   * 课程计划父级Id
   */
  private Long parentid;

  /**
   * 层级，分为1、2、3级
   */
  private Integer grade;

  /**
   * 课程类型:1视频、2文档
   */
  private String mediaType;


  /**
   * 课程标识
   */
  private Long courseId;

  /**
   * 课程发布标识
   */
  private Long coursePubId;


  /**
   * 是否支持试学或预览（试看）
   */
  private String isPreview;


}

