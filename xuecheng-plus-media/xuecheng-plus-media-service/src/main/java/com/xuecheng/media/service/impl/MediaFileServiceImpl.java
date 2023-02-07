package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @description 普通文件上传 service
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {
 @Autowired
 private MediaFilesMapper mediaFilesMapper;

 @Autowired
 private MinioClient minioClient;

// @Autowired
// private MediaFileService currentProxy;

 /**
  * 读取配置文件的 配置信息
  * 普通文件 桶的名称
  */
 @Value("${minio.bucket.files}")
 private String fileBucket;

 /**
  * 读取配置文件的 配置信息
  * 视频文件 桶的名称
  */
 @Value("${minio.bucket.videofiles}")
 private String videoBucket;


 /**
  * 查询媒资信息
  * @param companyId 公司id
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return
  */
 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  String filename = queryMediaParamsDto.getFilename();
  String fileType = queryMediaParamsDto.getFileType();
  queryWrapper.like(StringUtils.isNotBlank(filename), MediaFiles::getFilename, filename)
          .eq(StringUtils.isNotBlank(fileType), MediaFiles::getFileType, fileType);

  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

 /**
  *  上传 小文件
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
  * @return
  */
 @Override
 public UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
  // 1.1处理路径名
  if (StringUtils.isEmpty(folder)) {
   // 考虑用户不指定路径名的前提，利用日期生成文件名
   folder = getFileFolder(new Date(), true, true, true);
  } else if ('/' != folder.charAt(folder.length() - 1)) {
   // 考虑用户 路径名 未加 '/'的前提
   folder = folder + '/';
  }

  // 1.2处理文件名
  // 获取文件的md5值
  String fileMd5 = DigestUtils.md5Hex(bytes);
  // 获取上传的文件名称， 这里的名称是文件在客户端的名称， 下面的objectName 是 存储在mioio中的名称
  String filename = uploadFileParamsDto.getFilename();
  // 获取 文件后缀
  String extendName = filename.substring(filename.lastIndexOf("."));
  if (StringUtils.isEmpty(objectName)) {
   // 如果文件名为空，则使用文件的md5值为名称, 在加后缀
   objectName = fileMd5 + extendName;
  }

  // 1.3拼接文件名
  String realFileName = folder + objectName;

  // 1.4 根据文件扩展名获取 传输类型
  ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extendName);
  String contentType = extensionMatch.getMimeType();
  log.info("文件传输类型为{}", contentType);


  try {
   // 2. 上传文件至 minio
   addMediaFilesToMinIO(contentType, fileBucket,bytes, realFileName);

   // 3.上传到数据库   key 事务问题
   // 使用this 对象调用，事务失效
//   MediaFiles mediaFiles = this.addMediaFilesToDb(companyId, uploadFileParamsDto, fileMd5, realFileName);
   // 解法方法1：自己注入自己，生成代理对象，调用接口方法,事务成功
//   MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, uploadFileParamsDto, fileMd5, realFileName);
   // 解决方法2：利用AopContext 手动创建代理对象
   MediaFileService currentProxy = (MediaFileService) AopContext.currentProxy();
   MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileBucket, uploadFileParamsDto, fileMd5, realFileName);

   // 4.准备返回数据
   UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
   BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
   return uploadFileResultDto;
  } catch (Exception e) {
   e.printStackTrace();
   XueChengPlusException.exce("上传过程出错！");
  }
  return null;

 }


 /**
  *  将文件以字节流的方式 传至 minio
  * @param contentType 文件传输类型
  * @param bytes 文件字节信息
  * @param bucket 桶的名称
  * @param realFileName 拼接文件名称 minio用的
  */
 private void addMediaFilesToMinIO(String contentType, String bucket, byte[] bytes, String realFileName) {
  // 文件流
  ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

  try {
   // 2.上传到 minio
   PutObjectArgs putObjectArgs = PutObjectArgs.builder()
           .bucket(bucket)
           .object(realFileName)
           //InputStream stream, long objectSize 对象大小, long partSize 分片大小(-1 表示5M,最大不超过 5T, 最多 10000)
           .stream(inputStream, inputStream.available(), -1)
           .contentType(contentType)
           .build();

   minioClient.putObject(putObjectArgs);
   
  } catch (Exception e) {
   log.debug("上传文件失败{}", e.getMessage());
   XueChengPlusException.exce("service -- 上传文件出错！");
  }
 }


 /**
  *  将大文件 直接传递到 minio
  * @param bucket 桶的名字
  * @param object minio中 文件的名字
  * @param filePath 需要传递的文件的路径
  */
 private void addMediaFilesToMinIO(String bucket, String object, String filePath) {
  // 上传文件
  try {
   minioClient.uploadObject(
           UploadObjectArgs.builder()
                   .bucket(bucket)
                   // 同一个桶内 对象名不能重复；
                   // 重复文件会覆盖
                   .object(object)
                   .filename(filePath)
                   .build());
   log.info("文件-{}-上传成功！", filePath);
  } catch (Exception e) {
   XueChengPlusException.exce("文件上传出错！");
  }

 }



 /**
  *  将文件信息 写入数据库
  * @param companyId 机构id
  * @param bucket 桶的名称
  * @param uploadFileParamsDto 文件上传 参数信息
  * @param fileMd5 文件md5
  * @param minioFileName 拼接文件名称 minio用的
  * @return 整理好的文件信息
  */
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId, String bucket, UploadFileParamsDto uploadFileParamsDto, String fileMd5, String minioFileName) {
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if(mediaFiles != null) {
   // 不允许重复插入
   XueChengPlusException.exce("不允许重复插入文件！");
  }

  // 3.1拷贝基本信息
  mediaFiles = new MediaFiles();
  BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
  mediaFiles.setFileId(fileMd5);
  mediaFiles.setId(fileMd5);
  mediaFiles.setCompanyId(companyId);
  mediaFiles.setFilePath(minioFileName);
  mediaFiles.setUrl("/" + bucket + "/" + minioFileName);
  mediaFiles.setBucket(bucket);
  mediaFiles.setCreateDate(LocalDateTime.now());
  mediaFiles.setStatus("1");
  mediaFiles.setAuditStatus("002003");
  // 插入文件表
  int insert = mediaFilesMapper.insert(mediaFiles);
  if (insert < 0) {
   XueChengPlusException.exce("数据库插入出错！");
  }
  return mediaFiles;
 }


 /**
  * 文件上传前检查文件
  *   数据库和文件系统都存在，才认为存在
  *   只要有一个不存在，就认为不存在，可以重传
  * @param fileMd5 文件的 md5值
  * @return  只有当都存在时,才认为存在
  */
 @Override
 public RestResponse<Boolean> checkfile(String fileMd5) {
  // 1.查询数据库
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles == null) {
   // 数据库中没有信息
   return RestResponse.success(false);
  }

  // 2.查询文件系统
  String bucket = mediaFiles.getBucket();
  String filePath = mediaFiles.getFilePath();
  return queryMinio(bucket, filePath);

 }


 /**
  *  查询 minio 文件系统中 文件是否存在
  *    当文件不存在时，客户端会抛出异常
  * @param bucket 桶名称
  * @param filePath 文件名称
  * @return 不存在-flase  存在-true
  */
 private RestResponse<Boolean> queryMinio(String bucket, String filePath) {
  GetObjectArgs getObjectArgs = GetObjectArgs.builder()
          .bucket(bucket)
          .object(filePath)
          .build();
  try {
   // 当文件 不存在时 ，会抛出异常
    InputStream inputStream= minioClient.getObject(getObjectArgs);
    if (inputStream == null) {
     // 文件系统中不存在
     return RestResponse.success(false);
    }
  } catch (Exception e) {
   //e.printStackTrace();
   // 文件系统中不存在
   return RestResponse.success(false);
  }
  // 文件存在
  return RestResponse.success(true);
 }


 /**
  * 分块文件上传前的检测
  *   分块文件，只存在于文件系统中；数据库中不保存信息
  * @param fileMd5 分块文件的md5值
  * @param chunk 分块序号
  * @return 只要文件系统中有，就认为有
  */
 @Override
 public RestResponse<Boolean> checkchunk(String fileMd5, int chunk) {
  // 得到分块文件所在目录
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  // 得到分块文件的路径
  String chunkFilePath = chunkFileFolderPath + chunk;
  // 查询文件系统分块文件是否存在
  return queryMinio(videoBucket, chunkFilePath);
 }


 /**
  *  根据文件的md5值 生成分块文件目录
  *  video/b/b/bb5ffddff042536232d4caa0a48702ca/chunk/1
  *  video 是桶名称
  *  后面的 b,b是md5值的前2位
  *  在后面是 md5值
  *  在后面是固定的 chunk
  *  最后是 第几个分块
  * @param fileMd5 文件md5值
  * @return 利用文件md5值 创建多个分层，方便io操作
  */
 private String getChunkFileFolderPath(String fileMd5) {
  return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
 }

 /**
  * 根据文件的md5值 生成文件目录
  * video/b/b/bb5ffddff042536232d4caa0a48702ca/bb5ffddff042536232d4caa0a48702ca.mp4
  * @param fileMd5 文件md5
  * @param fileExt 文件扩展名
  * @return minio 存储路径
  */
 private String getFileFolderPath(String fileMd5, String fileExt) {
  return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
 }


 /**
  * 上传分块文件
  * @param bytes 分块文件的字节信息
  * @param fileMd5 分块文件的md5
  * @param chunk 块数
  * @return
  */
 @Override
 public RestResponse uploadchunk(byte[] bytes, String fileMd5, int chunk) {
  // 获取文件名称
  String fileName = getChunkFileFolderPath(fileMd5) + chunk;
  // 上传分块文件
  try {
   addMediaFilesToMinIO("application/octet-stream", videoBucket, bytes, fileName);
  } catch (Exception e) {
   log.info("异常信息-{}", e.getMessage());
   XueChengPlusException.exce("传输过程异常,请重试！");
  }

  return RestResponse.success();
 }


 /**
  *  合并分块文件
  *    key 流的使用,临时文件的删除，大量try
  * @param companyId 机构id
  * @param fileMd5 文件md5
  * @param chunkTotal 总块数
  * @param uploadFileParamsDto 文件上传信息
  * @return
  */
 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
  // 1.下载一个文件的 所有 分块文件
  File[] files = downloadFile(fileMd5, chunkTotal);


  // 2.合并分块文件
  // 得到文件扩展名
  String filename = uploadFileParamsDto.getFilename();
  String extension = filename.substring(filename.lastIndexOf("."));

  File mergeFile = null;
  try {
   // 2.1 创建临时合并文件
   try {
    // 创建一个临时的 合并文件 （只是占个坑，还没有数据）
    mergeFile = File.createTempFile("merge", extension);
   } catch (Exception e) {
    XueChengPlusException.exce("创建合并临时文件出错！");
   }

   // 2.2合并文件
   try(// 创建写文件对象，用于写文件
       RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");) {
    //指针指向文件顶端
    raf_write.seek(0);
    //缓冲区
    byte[] b = new byte[1024];
    //合并文件
    for (File chunkFile : files) {
     // 读取分块文件的 流文件
     try (RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");) {
      int len = -1;
      while ((len = raf_read.read(b)) != -1) {
       // 向合并文件中写入数据
       raf_write.write(b, 0, len);
      }
     }
    }
   } catch (IOException e) {
    e.printStackTrace();
    XueChengPlusException.exce("合并文件异常！");
   }
   log.info("合并文件完成{}",mergeFile.getAbsolutePath());
   // 设置文件大小，方便存入数据库
   uploadFileParamsDto.setFileSize(mergeFile.length());


   // 2.3校验文件
   try (
           FileInputStream mergeFileStream = new FileInputStream(mergeFile);
   ) {
     //取出合并文件的md5进行比较
     String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
     if (!fileMd5.equals(mergeFileMd5)) {
      log.info("合并文件校验不通过, 文件路径:{}, 原始文件md5:{}", mergeFile.getAbsolutePath(), fileMd5);
      XueChengPlusException.exce("合并文件校验不通过！");
     }
   } catch (Exception e) {
    log.info("合并文件校验出错, 文件路径:{}, 原始文件md5:{}", mergeFile.getAbsolutePath(), fileMd5);
    XueChengPlusException.exce("文件校验异常！");
   }


   // 3.上传完整文件
   String fileName = getFileFolderPath(fileMd5, extension);
   addMediaFilesToMinIO(videoBucket, fileName, mergeFile.getAbsolutePath());

   // 4.记录视频文件信息
   addMediaFilesToDb(companyId, videoBucket, uploadFileParamsDto, fileMd5, fileName);

   return RestResponse.success();
  } finally {
   // 删除临时分块文件
   if (files != null) {
    for (File file : files) {
     file.delete();
    }
   }
   // 删除合并文件
   if (mergeFile != null) {
    mergeFile.delete();
   }

  }
 }





 /**
  * 下载一个文件的所有分块文件
  * @param fileMd5 文件md5
  * @param chunkTotal 分块总数
  * @return 并以文件数组的形式返回
  */
 private File[] downloadFile(String fileMd5, int chunkTotal) {
  // 得到分块文件的总体路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  // 分块数组文件
  File[] chuckFiles = new File[chunkTotal];
  // 循环下载分块文件
  for (int i = 0; i < chunkTotal; i++) {
   // 获取 分块文件 名称
   String fileName = chunkFileFolderPath + i;

   // 创建分块临时文件
   File chuckFile = null;
   try {
    // 创建分块 临时文件  无数据，空占一个位置
    chuckFile = File.createTempFile("chuck", null);
   } catch (IOException e) {
    e.printStackTrace();
    XueChengPlusException.exce("创建分块临时文件异常！");
   }

   // 从文件系统下载 分块文件
   File file = downloadFileFromMinIO(fileName, chuckFile, videoBucket);
   chuckFiles[i] = file;

  }

  return chuckFiles;
 }



 /**
  *  从 文件系统 下载文件
  * @param object 文件名称
  * @param bucket 桶的名称
  * @param file 需要覆盖的文件，或者说下到哪
  */
 private File downloadFileFromMinIO(String object, File file, String bucket) {
  // 从文件系统 获取流
  GetObjectArgs getObjectArgs = GetObjectArgs.builder()
          .bucket(bucket)
          .object(object)
          .build();
  try ( // try()  括号里面 的流 用过一次就关闭了
        // 从文件系统获 取输入流
        InputStream inputStream = minioClient.getObject(getObjectArgs);
        // 创建 分块临时文件的 输出流
        FileOutputStream outputStream= new FileOutputStream(file);
  ){
   // 流的copy
   IOUtils.copy(inputStream, outputStream);
   // 将 有数据的分块文件放入数组
   return file;

  } catch (Exception e) {
   e.printStackTrace();
   XueChengPlusException.exce("下载文件异常！");
  }
  return null;
 }



 /**
  * 根据日期拼接目录
  * @param date 日期
  * @param year 是否需要年
  * @param month 是否需要月
  * @param day 是否需要日
  * @return  目录名称
  */
 private String getFileFolder(Date date, boolean year, boolean month, boolean day){
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  //获取当前日期字符串
  String dateString = sdf.format(new Date());
  //取出年、月、日
  String[] dateStringArray = dateString.split("-");
  StringBuilder folderString = new StringBuilder();
  if(year){
   folderString.append(dateStringArray[0]);
   folderString.append("/");
  }
  if(month){
   folderString.append(dateStringArray[1]);
   folderString.append("/");
  }
  if(day){
   folderString.append(dateStringArray[2]);
   folderString.append("/");
  }
  return folderString.toString();
 }

}
