package com.xuecheng.content.feignclient.SearchServiceClient;

import com.xuecheng.content.feignclient.model.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName SearchServiceClient
 * @Date 2023/2/10 14:10
 * @Author diane
 * @Description 搜索服务远程调用
 * @Version 1.0
 */
@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {
    /**
     * 添加课程索引
     * @param courseIndex
     * @return
     */
    @PostMapping("search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
