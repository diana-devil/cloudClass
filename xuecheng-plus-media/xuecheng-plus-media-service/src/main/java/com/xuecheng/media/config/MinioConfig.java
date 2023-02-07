package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName MinioConfig
 * @Date 2023/2/7 12:54
 * @Author diane
 * @Description minio配置
 * @Version 1.0
 */
@Configuration
public class MinioConfig {
    /**
     *  访问网址+端口
     */
    @Value("${minio.endpoint}")
    private String endpoint;
    /**
     * 账户
     */
    @Value("${minio.accessKey}")
    private String accessKey;
    /**
     * 密码
     */
    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
