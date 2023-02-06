package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
* @author 凉冰
* @description 针对表【course_teacher(课程-教师关系表)】的数据库操作Service实现
 *          不使用 mapper中的方法，全部使用 service中的方法
* @createDate 2023-01-31 09:55:38
*/
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher>
    implements CourseTeacherService {

//    @Resource
//    private CourseTeacherMapper courseTeacherMapper;

    /**
     * 获取 课程对应教师信息
     * @param courseId 课程id
     * @return 教师信息
     */
    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> query = new LambdaQueryWrapper<>();
        query.eq(CourseTeacher::getCourseId, courseId);
        return list(query);
    }


    /**
     *  新增/修改 师资信息
     *  根据是否包含 师资id 来区分是 新增还是修改
     * @param courseTeacher  师资信息
     */
    @Override
    public void saveCourseTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();
        // 新增 师资信息
        if (id == null) {
            courseTeacher.setCreateDate(LocalDate.now());
            try {
                save(courseTeacher);
            } catch (Exception e) {
                XueChengPlusException.exce("老师姓名重复！");
            }
        } else {
            // 修改师资信息
            // 更新的时候，要先查询出来，在此基础上进行修改
            CourseTeacher res = getById(id);
            BeanUtils.copyProperties(courseTeacher, res);
            try {
                updateById(res);
            } catch (Exception e) {
                XueChengPlusException.exce("老师姓名重复！");
            }
        }
    }

    /**
     *  删除师资信息
     * @param courseId 课程id
     * @param id 师资id
     */
    @Override
    public void removeCourseTeacher(Long courseId, Long id) {
        LambdaQueryWrapper<CourseTeacher> query = new LambdaQueryWrapper<>();
        query.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, id);
        boolean bool = remove(query);
        // 异常删除
        if (!bool) {
            XueChengPlusException.exce(CommonError.UNKOWN_ERROR);
        }
    }
}




