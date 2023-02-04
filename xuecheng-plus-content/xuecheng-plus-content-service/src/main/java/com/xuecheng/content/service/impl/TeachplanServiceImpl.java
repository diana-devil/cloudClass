package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 凉冰
* @description 针对表【teachplan(课程计划)】的数据库操作Service实现
* @createDate 2023-01-31 09:55:38
*/
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan>
    implements TeachplanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    /**
     * 查询课程计划树形结构
     * @param courseId 课程id
     * @return 课程计划树型结构dto
     */
    @Override
    public List<TeachplanDto> findTeachplayTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }


    /**
     * 修改或者新增课程计划
     *  根据是否包含  课程计划id 来区分 新增课程计划，还是修改课程计划
     * @param teachplanDto 课程计划信息
     */
    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        // 尝试获取 课程计划id
        Long id = teachplanDto.getId();

        // 新增课程计划,记得要修改 orderby 属性
        //  每次新增 大节或者小节的时候 orderby 都应该 比起之前的 +1
        if (id == null) {
            // 获取现有的 orderby 大小
            Long courseId = teachplanDto.getCourseId();
            Long parentid = teachplanDto.getParentid();
            LambdaQueryWrapper<Teachplan> query = new LambdaQueryWrapper<>();
            query.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentid);
            Integer count = teachplanMapper.selectCount(query);
            // 属性copy
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, teachplan);
            // 修改 orderby 字段值
            teachplan.setOrderby(count + 1);
            save(teachplan);
        } // 修改课程计划
        else {
            // 更新 课程计划表
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, teachplan);
            updateById(teachplan);

            // TODO 更新 课程计划-媒体表
        }

    }
}




