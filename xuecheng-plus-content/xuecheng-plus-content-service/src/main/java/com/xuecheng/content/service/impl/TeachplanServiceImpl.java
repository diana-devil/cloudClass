package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan>
    implements TeachplanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

    @Resource
    private CourseBaseMapper courseBaseMapper;


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
            // 更新的时候，要先查询出来，在此基础上进行修改
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto, teachplan);
            updateById(teachplan);

            // TODO 更新 课程计划-媒体表
        }

    }


    /**
     * 删除课程计划
     *    只有当课程状态是未提交时，方可删除
     *    删除第一级别的章时要求章下边没有小节方可删除。
     *    删除第二级别的小节的同时需要将其它关联的视频信息也删除。
     * @param id 课程计划id
     */
    @Override
    @Transactional
    public void removeTeachPlan(Long id) {
        // 0. 查询课程计划级别
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            return;
        }
        Integer grade = teachplan.getGrade();
        Long courseId = teachplan.getCourseId();

        // 1. 查询课程状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.exce(CommonError.UNKOWN_ERROR);
        }
        String auditStatus = courseBase.getAuditStatus();
        // 只有当课程是未提交时方可删除
        if (!"202002".equals(auditStatus)) {
            XueChengPlusException.exce("删除失败，课程审核状态是未提交时方可删除!");
        }

        // 2. 第二级别课程，先删除 视频关联表，在删除课程计划表
        if (grade == 2) {
            // 2.1 删除关联视频信息
            LambdaQueryWrapper<TeachplanMedia> query = new LambdaQueryWrapper<>();
            query.eq(TeachplanMedia::getTeachplanId, id);
            // 空删除会报错嘛？ -- 不会报错
            teachplanMediaMapper.delete(query);
            // 2.2 删除课程计划表
            removeById(id);
        }
        // 3. 第一节课程，查询其下的二级课程剩余数，当剩余数为0时，方可删除；否则，抛出异常
        else if (grade == 1) {
            LambdaQueryWrapper<Teachplan> query = new LambdaQueryWrapper<>();
            query.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, id).eq(Teachplan::getGrade, 2);
            Integer count = teachplanMapper.selectCount(query);
            log.info("剩余小节数量:{}", count);
            if (count > 0) {
                XueChengPlusException.exce("课程计划信息还有子级信息，无法操作");
            } else {
                removeById(id);
            }

        } else {
            // 未知章节，抛出 非法参数异常
            XueChengPlusException.exce(CommonError.PARAMS_ERROR);
        }



    }
}




