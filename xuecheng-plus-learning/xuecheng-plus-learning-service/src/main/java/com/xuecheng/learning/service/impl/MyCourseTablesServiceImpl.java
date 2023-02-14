package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableItemDto;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.xuecheng.base.constants.DataDictionary.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 16:12
 */
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesServiceImpl myCourseTablesService;

    /**
     *  添加选课 ，-- 更新 选课记录表，更新我的课程表
     * @param userId 用户id
     * @param courseId 课程id
     * @return 包含学习资格的选课表信息
     */
    @Override
    public XcChooseCourseDto addChooseCourse(String userId,Long courseId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish==null){
            XueChengPlusException.exce("课程信息不存在");
        }
        Long id = coursepublish.getId();
        if(id==null){
            XueChengPlusException.exce(CommonError.UNKOWN_ERROR);
        }
        //课程收费标准
        String charge = coursepublish.getCharge();

        XcChooseCourse xcChooseCourse = null;
        if(CHARGE_NOT_FREE.equals(charge)){
            //添加收费课程
            xcChooseCourse= myCourseTablesService.addChargeCoruse(userId,coursepublish);
        }else{
            //添加免费课程
            xcChooseCourse= myCourseTablesService.addFreeCoruse(userId,coursepublish);
        }

        // 更新选课 资格
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        //获取学习资格
        XcCourseTablesDto xcCourseTablesDto = getLeanringStatus(userId, courseId);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        log.info("添加选课成功！");
        return xcChooseCourseDto;
    }


    /**
     * @description 判断学习资格
     *  从我的课程表 查询数据
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @author Mr.M
     * @date 2022/10/3 7:37
    */
    public XcCourseTablesDto getLeanringStatus(String userId, Long courseId){
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        // 课程表为空，说明没有学习资格，
        if(xcCourseTables==null){
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            xcCourseTablesDto.setLearnStatus(STUDY_NOCOURSE);
            return xcCourseTablesDto;
        }
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        //是否过期,true过期，false未过期
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if(!isExpires){
            //正常学习
            xcCourseTablesDto.setLearnStatus(STUDY_NORMAL);
           return xcCourseTablesDto;

        }else{
            //已过期
            xcCourseTablesDto.setLearnStatus(STUDY_OVERDUE);
            return xcCourseTablesDto;
        }

    }


    /**
     * 保存选课状态
     * @param choosecourseId 选课id
     * @return
     */
    @Override
    public boolean saveChooseCourseStauts(String choosecourseId) {
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(choosecourseId);
        if(xcChooseCourse!=null){
            String status = xcChooseCourse.getStatus();
            if(COURSE_SELECTION_STATUS_NOT_PAY.equals(status)){//待支付
                //更新为选课成功
                xcChooseCourse.setStatus(COURSE_SELECTION_STATUS_SUCCESS);
                int update = xcChooseCourseMapper.updateById(xcChooseCourse);
                //添加到课程表
                addCourseTabls(xcChooseCourse);
                if(update>0){
                    log.debug("收到支付结果通知处理成功,选课记录:{}",xcChooseCourse);
                    return true;
                }else{
                    log.debug("收到支付结果通知处理失败,选课记录:{}",xcChooseCourse);
                    return false;
                }
            }else{
                log.debug("收到支付结果通知已经处理,选课记录:{}",xcChooseCourse);
                return true;
            }
        }else{
            log.debug("收到支付结果通知没有查询到关联的选课记录,choosecourseId:{}",choosecourseId);
        }
        return false;
    }


    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @author Mr.M
     * @date 2022/10/2 17:07
    */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;

    }


    @Transactional
    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        //查询选课记录表是否存在免费的且选课成功的订单
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, COURSE_SELECTION_TYPE_FREE)//免费订单
                .eq(XcChooseCourse::getStatus, COURSE_SELECTION_STATUS_SUCCESS);//选课成功
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            // 选课记录表有记录，不允许重复添加，直接返回
            // XueChengPlusException.exce("已经添加选课，无需再次添加");
            return xcChooseCourses.get(0);
        }
        // 向选课记录表 xc_choose_course，添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType(COURSE_SELECTION_TYPE_FREE);//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus(COURSE_SELECTION_STATUS_SUCCESS);//选课成功
        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);

        //向课程表 xc_course_tables，添加到我的课程表
        addCourseTabls(xcChooseCourse);
        return xcChooseCourse;

    }


    /**
     * @description 添加到我的课程表
     * @param xcChooseCourse 选课记录
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @author Mr.M
     * @date 2022/10/3 11:24
    */
    @Transactional
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!COURSE_SELECTION_STATUS_SUCCESS.equals(status)){
            XueChengPlusException.exce("选课记录未完成，无法添加到课程表");
        }
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(xcCourseTables!=null){
            LocalDateTime validtimeEnd = xcChooseCourse.getValidtimeEnd();
            if(xcCourseTables.getValidtimeEnd().isAfter(validtimeEnd)){
                //如果我的课程表中的过期时间比新订单的过期时间靠后，不用更新课程表。
                return xcCourseTables;
            }else{
                //更新我的课程表
                xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
                xcCourseTables.setUpdateDate(LocalDateTime.now());
                xcCourseTables.setValidtimeStart(xcChooseCourse.getValidtimeStart());
                xcCourseTables.setValidtimeEnd(xcChooseCourse.getValidtimeStart());
                xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
                xcCourseTablesMapper.updateById(xcCourseTables);
                return xcCourseTables;
            }
        }

        // 如果是空，复制属性，插入我的课程表
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTablesMapper.insert(xcCourseTablesNew);

        return xcCourseTablesNew;

    }


    @Transactional
    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){

        //如果存在待支付记录直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, COURSE_SELECTION_TYPE_NOT_FREE)//收费订单
                .eq(XcChooseCourse::getStatus, COURSE_SELECTION_STATUS_NOT_PAY);//待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            // XueChengPlusException.exce("已经添加选课，无需再次添加");
            return xcChooseCourses.get(0);
        }

        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType(COURSE_SELECTION_TYPE_NOT_FREE);//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus(COURSE_SELECTION_STATUS_NOT_PAY);//待支付

        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }

    /**
     * 查询我的课程表
     * @param params 课程表参数
     * @return 分页参数
     */
    public PageResult<MyCourseTableItemDto> mycourestabls( MyCourseTableParams params){

        int page = params.getPage();
        int size = params.getSize();
        int startIndex = (page-1)*size;
        params.setStartIndex(startIndex);

        List<MyCourseTableItemDto> myCourseTableItemDtos = xcCourseTablesMapper.myCourseTables(params);
        int total = xcCourseTablesMapper.myCourseTablesCount(params);


        PageResult pageResult = new PageResult();
        pageResult.setItems(myCourseTableItemDtos);
        pageResult.setCounts(total);
        pageResult.setPage(page);
        pageResult.setPageSize(size);
        return pageResult;

    }

}
