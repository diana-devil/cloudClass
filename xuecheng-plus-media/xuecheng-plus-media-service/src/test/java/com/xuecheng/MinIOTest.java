package com.xuecheng;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName MinIOTest
 * @Date 2023/2/7 11:33
 * @Author diane
 * @Description 测试 分布式文件系统的  上传，下载，删除
 *      key 设计到 文件流 的操作
 * @Version 1.0
 */
public class MinIOTest {

    // 创建链接客户端
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.159.100:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();


    // 上传文件
    public static void upload()throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {

            //上传1.mp4
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            // 同一个桶内 对象名不能重复；
                            // 重复文件会覆盖
                            .object("1.mp4")
                            .filename("D:\\javaMiddleWare\\minio\\video\\1.mp4")
                            .build());
            // 上传成功后可以 在网站直接查看
            // http://192.168.159.100:9000/testbucket/1.mp4

            //上传diana.txt
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            // 子目录会自动创建
                            .object("txt/diana.txt")
                            .filename("D:\\javaMiddleWare\\minio\\diana.txt")
                            .build());
            System.out.println("上传成功");
            // 上传成功后可以 在网站直接查看
            // http://192.168.159.100:9000/testbucket/txt/diana.txt

        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }

    }

    // 删除文件
    public static void delete(String bucket,String filepath)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(filepath).build());
            System.out.println("删除成功");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }
    }


    //下载文件
    public static void getFile(String bucket,String filepath,String outFile)throws IOException, NoSuchAlgorithmException, InvalidKeyException {

            // try()  括号里面 的流 用过一次就关闭了
            try (   // 将minio中的文件，转换成输入流
                    InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filepath)
                            .build());
                    // 创建输出流
                    FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            ) {
                // Read data from stream
                // 流的copy , copy完成后，会将文件(filepath)保存在指定 路径(outFile)下
                IOUtils.copy(stream,fileOutputStream);
                System.out.println("下载成功");
            } catch (MinioException e) {
                System.out.println("Error occurred: " + e);
                System.out.println("HTTP trace: " + e.httpTrace());
            }

    }


    public static void main(String[] args)throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        // 上传文件
//        upload();

        // 删除文件
//        delete("testbucket","1.mp4");
//        delete("testbucket","txt/diana.txt");

        // 下载文件
        getFile("testbucket", "1.mp4", "D:\\javaMiddleWare\\minio\\video\\1-1.mp4");

    }

}
