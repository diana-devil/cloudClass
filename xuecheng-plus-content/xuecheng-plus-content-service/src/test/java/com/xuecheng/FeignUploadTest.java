package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

/**
 * @ClassName FeignUploadTest
 * @Date 2023/2/9 22:25
 * @Author diane
 * @Description 远程调用测试
 * @Version 1.0
 */
@SpringBootTest(classes = ContentServiceApplication.class)
public class FeignUploadTest {

    @Resource
    private MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\test.html"));
        mediaServiceClient.upload(multipartFile,"course","test.html");
    }

    //远程调用异常, 异常信息
    //com.netflix.client.ClientException: Load balancer does not have available server for client: media-api
    // 使用了熔断降级处理 之后的反馈

}



