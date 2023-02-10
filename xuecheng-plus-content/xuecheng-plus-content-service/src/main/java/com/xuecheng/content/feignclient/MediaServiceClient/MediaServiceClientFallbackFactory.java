package com.xuecheng.content.feignclient.MediaServiceClient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName MediaServiceClientFallbackFactory
 * @Date 2023/2/10 10:11
 * @Author diane
 * @Description 上级降级处理的方法
 *      实现FallbackFactory解口,重写降级方法
 *      优点：可以捕获下级的异常信息，推荐
 * @Version 1.0
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient>{

    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            // 实现降级处理
            @Override
            public String upload(MultipartFile upload, String folder, String objectName) {
                log.info("远程调用异常, 异常信息-{}", throwable.getMessage());
                return null;
            }
        };
    }
}
