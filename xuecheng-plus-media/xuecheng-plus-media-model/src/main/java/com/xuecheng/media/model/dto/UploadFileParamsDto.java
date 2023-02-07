package com.xuecheng.media.model.dto;

import lombok.Data;

/**
 * @ClassName UploadFileParamsDto
 * @Date 2023/2/7 12:52
 * @Author diane
 * @Description 上传普通文件 请求参数
 * @Version 1.0
 */
@Data
public class UploadFileParamsDto {
  /**
   * 文件名称
   */
  private String filename;

  /**
   * 文件content-type
   */
  private String contentType;

  /**
   * 文件类型（文档，音频，视频）
   */
  private String fileType;
  /**
   * 文件大小
   */
  private Long fileSize;

  /**
   * 标签
   */
  private String tags;

  /**
   * 上传人
   */
  private String username;

  /**
   * 备注
   */
  private String remark;

}
