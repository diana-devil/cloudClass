package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    // select * form media_process where id % 2 = 0 limit 6

    /**
     *  给每个任务 执行器 分配 一个任务集合
     * @param shardTotal 执行器总数
     * @param shardIndex 执行器编号
     * @param count 一次性获取任务 限制
     * @return 任务集合
     */
    @Select("select * from media_process where id % #{shardTotal} = #{shardIndex} and status = '1' limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex")int shardIndex, @Param("count")int count);


}
