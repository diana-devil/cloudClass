package com.xuecheng.content.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
* @author 凉冰
* @description 针对表【teachplan(课程计划)】的数据库操作Mapper
* @createDate 2023-01-31 09:55:38
* @Entity generator.domain.Teachplan
*/
public interface TeachplanMapper extends BaseMapper<Teachplan> {


    /**
     * 查询某课程的课程计划，组成树型结构
     *  自表链接，形成递归，从mapper层解决数据问题
     * @param courseId 课程id
     * @return 课程计划树型结构dto 列表
     */
    public List<TeachplanDto> selectTreeNodes(long courseId);

}




