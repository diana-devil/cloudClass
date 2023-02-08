package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.po.TeachplanMedia;


/**
* @author 凉冰
* @description 针对表【teachplan_media】的数据库操作Service
* @createDate 2023-01-31 09:55:38
*/
public interface TeachplanMediaService extends IService<TeachplanMedia> {


    /**
     * 教学计划绑定媒资信息
     * @param bindTeachplanMediaDto 绑定信息dto
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     *  删除绑定的关系
     * @param teachplanId 课程计划id
     * @param mediaId  媒资id
     */
    void removeAssociationMedia(Long teachplanId, String mediaId);
}
