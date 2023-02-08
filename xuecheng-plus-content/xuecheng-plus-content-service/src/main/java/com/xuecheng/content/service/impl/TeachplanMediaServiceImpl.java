package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanMediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
* @author 凉冰
* @description 针对表【teachplan_media】的数据库操作Service实现
* @createDate 2023-01-31 09:55:38
*/
@Service
@Slf4j
public class TeachplanMediaServiceImpl extends ServiceImpl<TeachplanMediaMapper, TeachplanMedia>
    implements TeachplanMediaService {

    @Resource
    private TeachplanMapper teachplanMapper;

    /**
     * 教学计划绑定媒资信息
     * @param dto 绑定信息dto
     */
    @Override
    @Transactional
    public void associationMedia(BindTeachplanMediaDto dto) {
        // 1. 约束校验
        Long teachplanId = dto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 教学计划不存在无法绑定
        if (teachplan == null) {
            XueChengPlusException.exce("课程计划为空！");
        }
        // 只有二级目录才可以绑定视频
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            XueChengPlusException.exce("只有二级目录才能绑定媒资信息！");
        }

        // 2. 删除原来的绑定关系  无论有没有 删除都可以正常走
        LambdaQueryWrapper<TeachplanMedia> query = new LambdaQueryWrapper<>();
        query.eq(TeachplanMedia::getTeachplanId, teachplanId);
        remove(query);


        // 3. 若不存在，往表中插入数据
        // 补全信息
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(dto, teachplanMedia);
        teachplanMedia.setMediaFilename(dto.getFileName());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        // 根据课程计划id, 找到课程id
        teachplanMedia.setCourseId(teachplan.getCourseId());
        // 插入数据库
        if (!save(teachplanMedia)) {
            XueChengPlusException.exce("保存绑定关系出错！");
        }

    }


    /**
     * 删除绑定关系
     * @param teachplanId 课程计划id
     * @param mediaId  媒资id
     */
    @Override
    public void removeAssociationMedia(Long teachplanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> query = new LambdaQueryWrapper<>();
        query.eq(TeachplanMedia::getMediaId, mediaId).eq(TeachplanMedia::getTeachplanId, teachplanId);
        if (!remove(query)) {
            XueChengPlusException.exce("删除绑定关系失败");
        }
    }
}




