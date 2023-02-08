package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName IMediaProcessService
 * @Date 2023/2/8 15:30
 * @Author diane
 * @Description 视频处理接口
 * @Version 1.0
 */
public interface IMediaProcessService {
    /**
     *  给每个任务 执行器 分配 一个任务集合
     * @param shardTotal 执行器总数
     * @param shardIndex 执行器编号
     * @param count 一次性获取任务 限制
     * @return 任务集合
     */
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex")int shardIndex, @Param("count")int count);


    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}
