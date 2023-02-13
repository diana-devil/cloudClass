package com.xuecheng.ucenter.feignClient.CheckCodeClient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName CheckCodeClientFallbackFactory
 * @Date 2023/2/11 16:34
 * @Author diane
 * @Description 降级服务
 * @Version 1.0
 */
@Slf4j
@Component
public class CheckCodeClientFallbackFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key_code, String code) {
                log.info("验证码服务远程调用失败，原因:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
