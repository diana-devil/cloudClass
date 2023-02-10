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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.xuecheng.base.constants.DataDictionary.AUDIT_UNCOMMITTED;

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
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
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
            // 获取现有的 最大的 orderby 大小 ,正序排列
            List<Teachplan> teachplans = getSameGradeTeachplans(teachplanDto.getCourseId(), teachplanDto.getParentid(), 1);
            Integer orderby;
            if (teachplans.size() > 0) {
                orderby = teachplans.get(teachplans.size() - 1).getOrderby();
            } else { // 之前没有小节，第一次添加
                orderby = 0;
            }
            log.info("当前最大的orderby为{}", orderby);

            // 属性copy
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto, teachplan);
            // 修改 orderby 字段值
            teachplan.setOrderby(orderby + 1);
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
     * 查询同级别的 课程计划
     * @param courseId 课程id
     * @param parentId 父课程id
     * @param flag 排序方式 1表示正序排列；0表示倒序排列
     * @return 按照orderby字段排好序之后的，课程计划列表
     */
    private List<Teachplan> getSameGradeTeachplans(Long courseId, Long parentId, int flag) {
        LambdaQueryWrapper<Teachplan> query = new LambdaQueryWrapper<>();
        query.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentId);
        if (flag == 1) {
            query.orderByAsc(Teachplan::getOrderby);
        } else if (flag == 0) {
            query.orderByDesc(Teachplan::getOrderby);
        } else {
            XueChengPlusException.exce(CommonError.UNKOWN_ERROR);
        }
        return teachplanMapper.selectList(query);
    }


    /**
     * 删除课程计划
     *    只有当课程状态是未提交时，方可删除
     *    删除第一级别的章时要求章下边没有小节方可删除。
     *    删除第二级别的小节的同时需要将其它关联的视频信息也删除。
     *    要修改课程的orderby
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
        if (!AUDIT_UNCOMMITTED.equals(auditStatus)) {
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


    /**
     * 移动课程计划
     *      向上移动(moveup)表示和上边的课程计划交换位置，将两个课程计划的排序字段值交换。
     *      向下移动(movedown)表示和下边的课程计划交换位置，将两个课程计划的排序字段值交换。
     * @param moveType 移动类型
     * @param id 课程计划id
     */
    @Override
    public void moveTeachPlan(String moveType, Long id) {
        // 获取当前 orderby值
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            XueChengPlusException.exce(CommonError.UNKOWN_ERROR);
        }
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();
        List<Teachplan> teachplans = new ArrayList<>();

        // 向上移动
        if ("moveup".equals(moveType)) {
            // 获取当前同级别的 课程计划  降序排列
            teachplans = getSameGradeTeachplans(courseId, parentid, 0);
        }
        // 向下移动
        else if ("movedown".equals(moveType)) {
            // 获取当前同级别的 课程计划  升序排列
            teachplans = getSameGradeTeachplans(courseId, parentid, 1);
        }
        // 其他情况
        else {
            XueChengPlusException.exce(CommonError.PARAMS_ERROR);
        }

        // 同级别课程计划小于等于1，不做处理
        if (teachplans.size() <= 1) {
            return;
        }
        // 遍历列表,找到 当前课程计划，和其下面的课程计划
        Iterator<Teachplan> iterator = teachplans.iterator();
        while(iterator.hasNext()) {
            Teachplan next = iterator.next();
            if (next.getId().equals(id)) {
                if (!iterator.hasNext()) {
                    XueChengPlusException.exce("已经在最边缘，无需移动！");
                }
                // 交换 两个 课程计划的 orderby
                swapTeachplan(next, iterator.next());
            }
        }
    }


    /**
     *  交换两个课程计划的orderby
     * @param next 课程计划1
     * @param snext 课程计划2
     */
    @Transactional
    public void swapTeachplan(Teachplan next, Teachplan snext) {
        Integer orderby = next.getOrderby();
        Integer sorderby = snext.getOrderby();
        next.setOrderby(sorderby);
        snext.setOrderby(orderby);
        // 更新数据库
        boolean b = updateById(next);
        boolean b1 = updateById(snext);
        if (!(b && b1)) {
            XueChengPlusException.exce(CommonError.UNKOWN_ERROR);
        }
    }
}




