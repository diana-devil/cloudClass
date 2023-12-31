package com.xuecheng.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableItemDto;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @description 我的课程表service接口
 * @author Mr.M
 * @date 2022/10/2 16:07
 * @version 1.0
 */
public interface MyCourseTablesService extends IService<XcChooseCourse> {

    /**
     * 添加选课
     * @param userId 用户id
     * @param courseId 课程id
     * @return
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * @description 判断学习资格
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @author Mr.M
     * @date 2022/10/3 7:37
     */
    public XcCourseTablesDto getLeanringStatus(String userId, Long courseId);


    /**
     * 更新 选课状态为 选课成功，并将课程插入 我的课程表
     * @param choosecourseId 选课id
     * @return
     */
    public boolean saveChooseCourseStauts(String choosecourseId);


    /**
     * 查询我的课程表
     * @param params 课程表参数
     * @return 分页参数
     */
    public PageResult<MyCourseTableItemDto> mycourestabls(MyCourseTableParams params);


    /**
     * 向我的课程表添加 信息
     * @param xcChooseCourse 我的课表信息
     * @return
     */
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse);

}
