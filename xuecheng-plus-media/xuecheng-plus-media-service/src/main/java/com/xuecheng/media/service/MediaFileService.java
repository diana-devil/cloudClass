package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


 /**
  * key  通用service 设计思想
  * 定义 通用的 文件上传 方法，与框架无关； 更通用
  * @param companyId 机构id
  * @param uploadFileParamsDto 文件上传信息
  * @param bytes 文件字节数组
  *       controller 层是用的 MultipartFile 类型，是由 springmvc框架定义的；
  *       这里用 字节数组的形式，屏蔽了上层接口，更加通用
  *
  * @param folder 子文件夹目录
  * @param objectName 文件名称
  *      这两个合起来才是 minio的文件名称，分开写，方便用户操作
  *
  * @return 文件上传结果dto
  */
 UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

 /**
  *  将文件信息 写入数据库
  *
  *  将方法 写成接口，可以通过注入自己的方式，使事务生效
  * @param companyId 机构id
  * @param bucket 桶的名称
  * @param uploadFileParamsDto 文件上传 参数信息
  * @param fileMd5 文件md5
  * @param minioFileName 拼接文件名称 minio用的
  * @return 整理好的文件信息
  */
 MediaFiles addMediaFilesToDb(Long companyId, String bucket, UploadFileParamsDto uploadFileParamsDto, String fileMd5, String minioFileName);

 /**
  * 文件上传前检查文件
  * @param fileMd5 文件的 md5值
  * @return 是否存在？ 是-true；否-flase
  */
 RestResponse<Boolean> checkfile(String fileMd5);

 /**
  * 分块文件上传前的检测
  * @param fileMd5 分块文件的md5值
  * @param chunk 分块序号
  * @return 是否存在？ 是-true；否-flase
  */
 RestResponse<Boolean> checkchunk(String fileMd5, int chunk);

 /**
  * 上传分块文件
  * @param bytes 分块文件的字节信息
  * @param fileMd5 分块文件的md5
  * @param chunk 块数
  * @return
  */
 RestResponse uploadchunk(byte[] bytes, String fileMd5, int chunk);


 /**
  * 合并文件
  * @param companyId 机构id
  * @param fileMd5 文件md5
  * @param chunkTotal 总块数
  * @param uploadFileParamsDto 文件上传信息
  * @return
  */
 RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);
}
