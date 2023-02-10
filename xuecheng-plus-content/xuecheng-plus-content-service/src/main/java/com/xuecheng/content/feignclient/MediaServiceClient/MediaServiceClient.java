package com.xuecheng.content.feignclient.MediaServiceClient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName MediaServiceClient
 * @Date 2023/2/9 22:06
 * @Author diane
 * @Description 媒资管理服务远程接口
 *      key 使用feign优雅的完成微服务之间的远程调用
 *      1. 配置feign客户端，注意是要访问的微服务名称和一些配置
 *      2. 配置将要访问的微服务的接口
 *      3. 添加允许注解
 * @Version 1.0
 */
// 第一个参数是 是链接的 微服务的名称；
// 第二个参数配置的是使 要传递的文件转换成  MultipartFile格式
// 第三个参数 配置的是 熔断降级的处理  -fallbackFactory模式
@FeignClient(value = "media-api",
        configuration = MultipartSupportConfig.class,
        fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    /**
     * 上传文件
     *
     * @param upload  上传文件
     * @param folder  文件目录
     * @param objectName 文件名称
     *  文件目录+文件名称 = minio的文件名
     * @return JSON 字符串
     */
    // 第一个是 响应路径 ；  第二个是 指定处理请求的提交内容类型
    @RequestMapping(value = "/media/upload/coursefile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName);

}
