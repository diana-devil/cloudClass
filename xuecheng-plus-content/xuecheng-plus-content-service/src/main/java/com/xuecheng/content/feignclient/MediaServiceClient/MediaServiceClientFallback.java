package com.xuecheng.content.feignclient.MediaServiceClient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName MediaServiceClientFallback
 * @Date 2023/2/10 10:06
 * @Author diane
 * @Description 上级降级处理的方法
 *      实现feign客户端接口,重写降级方法
 *      缺点：无法捕获下级的异常信息，不推荐
 * @Version 1.0
 */
public class MediaServiceClientFallback implements MediaServiceClient {

    @Override
    public String upload(MultipartFile upload, String folder, String objectName) {
        return null;
    }
}
