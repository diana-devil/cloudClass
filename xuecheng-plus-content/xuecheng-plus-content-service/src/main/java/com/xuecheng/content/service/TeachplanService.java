package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;


/**
* @author 凉冰
* @description 针对表【teachplan(课程计划)】的数据库操作Service
* @createDate 2023-01-31 09:55:38
*/
public interface TeachplanService extends IService<Teachplan> {

    /**
     * 查询课程计划树形结构
     * @param courseId 课程id
     * @return 课程计划树型结构dto
     */
    List<TeachplanDto> findTeachplayTree(Long courseId);

    /**
     *  修改或者新增课程计划
     * @param saveTeachplanDto 课程计划信息
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     *  删除课程计划
     * @param id 课程计划id
     */
    void removeTeachPlan(Long id);
}
