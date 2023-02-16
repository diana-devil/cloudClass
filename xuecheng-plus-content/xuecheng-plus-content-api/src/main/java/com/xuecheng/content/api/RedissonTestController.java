package com.xuecheng.content.api;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Queue;

/**
 * @ClassName RedissonTestController
 * @Date 2023/2/15 20:08
 * @Author diane
 * @Description redisson 入门测试
 *      可以将jvm 内存中的数据 存到 redis中，实现分布式
 *      测试方式： 创建一个队列，让两个不同的进程去访问它
 *              从结果可以看到，两个进程访问的是一个队列。
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/redisson")
public class RedissonTestController {

    @Autowired
    private RedissonClient redissonClient;

    // 入队
    @GetMapping("/joinqueue")
    public Queue<String> joinqueue (String queueStr) {
        // 判断队列是否存在，若不存在从redis中创建队列
        RQueue<String> queue = redissonClient.getQueue("queue001");
        queue.add(queueStr);
        return queue;
    }

    // 出队
    @GetMapping("/removequeue")
    public String removequeue () {
        // 从jvm内存中 取队列，并放到 redis中
        RQueue<String> queue = redissonClient.getQueue("queue001");
        return queue.poll();
    }

}
