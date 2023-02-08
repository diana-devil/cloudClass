package com.xuecheng.media.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 媒资文件管理接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
 @Slf4j
public class MediaFilesController {
    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);
    }


    /**
     * 上传文件
     *     传递的信息 不是简单的 json串
     *     而是 文件 大信息
     *     key 注意 参数的使用
     * @param upload  上传文件
     * @param folder  文件目录
     * @param objectName 文件名称
     * @return 上传文件结果 dto
     */
    @ApiOperation("上传小文件")
    // 第一个是 响应路径 ；  第二个是 指定处理请求的提交内容类型
    @RequestMapping(value = "/upload/coursefile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName) {

        Long companyId = 1232141425L;

        // 创建 传入参数 对象
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        String contentType = upload.getContentType();
        uploadFileParamsDto.setFileSize(upload.getSize());
        if(contentType.contains("image")){
            //图片
            uploadFileParamsDto.setFileType("001001");
        }else{
            //其它
            uploadFileParamsDto.setFileType("001003");
        }
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setTags("课程图片");
        uploadFileParamsDto.setFilename(upload.getOriginalFilename());
        log.info("原始文件名称--{}", upload.getOriginalFilename());
        uploadFileParamsDto.setContentType(contentType);


        // 获取 返回对象
        UploadFileResultDto uploadFileResultDto = null;
        try {
            uploadFileResultDto =  mediaFileService.upload(companyId, uploadFileParamsDto, upload.getBytes(), folder, objectName);
        } catch (Exception e) {
            XueChengPlusException.exce("controller - 上传文件出错！");

        }
        return uploadFileResultDto;

    }



    @ApiOperation("预览文件")
    @GetMapping("preview/{id}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String id) {
        MediaFiles mediaFiles = mediaFileService.getFileById(id);
        return RestResponse.success(mediaFiles.getUrl());
    }


}
