package com.xuecheng.media.service.impl;

import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.IMediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName MediaProcessServiceImpl
 * @Date 2023/2/8 15:30
 * @Author diane
 * @Description 视频处理接口实现类
 * @Version 1.0
 */
@Service
@Slf4j
public class MediaProcessServiceImpl implements IMediaProcessService {

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MediaProcessHistoryMapper mapper;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    /**
     *给每个任务 执行器 分配 一个任务集合
     * @param shardTotal 执行器总数
     * @param shardIndex 执行器编号
     * @param count 一次性获取任务 限制
     * @return
     */
    @Override
    public List<MediaProcess> selectListByShardIndex(int shardTotal, int shardIndex, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    /**
     * 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     */
    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 1. 查询任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            log.info("任务不存在！");
            return;
        }
        // 2. 根据任务状态判断 是否成功  2-成功 3-失败
        // 3. 失败,存储失败信息
        if ("3".equals(status)) {
            mediaProcess.setErrormsg(errorMsg);
            mediaProcess.setStatus("3");
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        // 4. 成功,更新历史信息表,删除原表
        if ("2".equals(status)) {
            // 4.1更新原表
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcess.setStatus("2");
            mediaProcessMapper.updateById(mediaProcess);

            // 4.2保存历史信息表
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();

            log.info(mediaProcess.toString());
            BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
            log.info(mediaProcessHistory.toString());
            log.info(mediaProcessHistory.getId().toString());

            int insert = mapper.insert(mediaProcessHistory);
            // 4.3删除原表
            int delete = mediaProcessMapper.deleteById(taskId);
            // 4.4更新媒资表 对应视频文件的url
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            if (mediaFiles == null) {
                log.info("该视频的基本信息不存在！");
                XueChengPlusException.exce("该视频的基本信息不存在！");
                return;
            }
            mediaFiles.setUrl(url);
            int i = mediaFilesMapper.updateById(mediaFiles);
            // 判断
            if (!(insert > 0 && delete > 0 && i > 0)) {
                log.info("更新视频处理表异常！");
                XueChengPlusException.exce("更新视频处理表异常！");
            }
        }
        // 5. 其他异常情况
        else {
            log.info("保存任务状态异常");
            XueChengPlusException.exce(CommonError.PARAMS_ERROR);
        }
    }
}
