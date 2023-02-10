package com.xuecheng.content.feignclient.SearchServiceClient;

import com.xuecheng.content.feignclient.model.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName SearchServiceClientFallbackFactory
 * @Date 2023/2/10 14:16
 * @Author diane
 * @Description 上级降级处理的方法
 *          实现熔断降级处理
 * @Version 1.0
 */
@Component
@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient>{

    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            // 实现降级处理
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.info("远程调用异常, 异常信息-{}", throwable.getMessage());
                return null;
            }
        };
    }
}
