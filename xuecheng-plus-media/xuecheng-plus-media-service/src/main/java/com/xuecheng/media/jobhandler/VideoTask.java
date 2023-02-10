package com.xuecheng.media.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.IMediaProcessService;
import com.xuecheng.media.service.impl.MediaFileServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName VideoTask
 * @Date 2023/2/8 16:33
 * @Author diane
 * @Description 视频处理任务
 * @Version 1.0
 */
@Component
@Slf4j
public class VideoTask {

    @Resource
    private IMediaProcessService mediaProcessService;

    @Resource
    private MediaFileServiceImpl mediaFileService;


//    private static Logger logger = LoggerFactory.getLogger(SampleXxlJob.class);

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;


    /**
     * 分片广播任务
     */
    @XxlJob("videoJobHander")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
//        log.info("log--分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

        // 1. 读取待处理任务
        List<MediaProcess> mediaProcessList = mediaProcessService.selectListByShardIndex(shardTotal, shardIndex, 2);
        if (mediaProcessList == null || mediaProcessList.size() <= 0) {
            log.info("没有待处理任务");
            log.info("任务执行结束！");
            return;
        }


        // 2. 启动size个线程池去处理
        // 获取待处理任务数量
        int size = mediaProcessList.size();
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        // 创建计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);


        // 遍历 列表, 将任务分配给线程池
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(()->{
                //桶
                String bucket = mediaProcess.getBucket();
                //存储路径
                String filePath = mediaProcess.getFilePath();
                //原始视频的md5值
                String fileId = mediaProcess.getFileId();
                //原始文件名称
                String filename = mediaProcess.getFilename();

                // 执行任务前，检测执行状态，保证幂等性
                String status = mediaProcess.getStatus();
                if ("2".equals(status)) {
                    log.info("该视频:{}已经处理过", mediaProcess);
                    // 计数器减一
                    countDownLatch.countDown();
                    return;
                }

                // 3. 下载视频
                // 创建下载用的临时文件  和  从avi 转换成的 mp4文件
                File tempFile = null;
                File mp4File = null;
                try {
                    tempFile = File.createTempFile("pending", null);
                    mp4File = File.createTempFile("MP4", ".mp4");
                } catch (IOException e) {
                    // 计数器减一
                    countDownLatch.countDown();
                    log.info("创建临时文件出错！");
                    e.printStackTrace();
                }
                // 从minio 文件系统中下载视频
                mediaFileService.downloadFileFromMinIO(filePath, tempFile, bucket);

                // 4. 调用工具类将avi格式转换成mp4格式
                //创建工具类对象
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, tempFile.getAbsolutePath(), mp4File.getAbsolutePath());
                log.info("生成的MP4文件-{}",mp4File.getAbsolutePath());

                //开始视频转换，成功将返回success
                String res = videoUtil.generateMp4();
                String statusNew = "3";
                String url = null;
                if ("success".equals(res)) {
                    log.info("视频转换成功！");
                    // 5. 将处理好的视频上传至mino
                    String object = mediaFileService.getFileFolderPath(fileId, ".mp4");
                    mediaFileService.addMediaFilesToMinIO(bucket, object, mp4File.getAbsolutePath());
                    statusNew = "2";
                    // 构造url
                    url = "/" + bucket + "/" + object;
                }

                // 6. 记录处理结果  无论 转换成功还是失败
                try {
                    mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), statusNew, fileId, url, res);
                } catch (Exception e) {
                    // 计数器减一
                    countDownLatch.countDown();
                    e.printStackTrace();
                }

                // 计数器减一
                countDownLatch.countDown();
            });
        });


        // 阻塞到任务完成
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);

        log.info("任务执行结束！");

    }


}
