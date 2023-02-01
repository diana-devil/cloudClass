package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
* @author 凉冰
* @description 针对表【course_base(课程基本信息)】的数据库操作Service实现
* @createDate 2023-01-31 09:55:38
*/
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase>
    implements CourseBaseService {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {

        // 分页参数
        IPage<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 课程名称模糊查询
        String courseName = queryCourseParams.getCourseName();
        //第一个条件是判空
        queryWrapper.like(StringUtils.isNotBlank(courseName),CourseBase::getName, courseName);
        // 课程审核状态
        String auditStatus = queryCourseParams.getAuditStatus();
        queryWrapper.eq(StringUtils.isNotBlank(auditStatus),CourseBase::getAuditStatus, auditStatus);
        // 课程发布状态
        String publishStatus = queryCourseParams.getPublishStatus();
        queryWrapper.eq(StringUtils.isNotBlank(publishStatus),CourseBase::getStatus, publishStatus);


        //E page 分页参数, @Param("ew") Wrapper<T> queryWrapper 查询条件
        IPage<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);


        // 拼装返回结果
        PageResult<CourseBase> result = new PageResult<CourseBase>();
        //总记录数
        result.setCounts(pageResult.getTotal());
        // 数据列表
        result.setItems(pageResult.getRecords());
        result.setPage(pageParams.getPageNo());
        result.setPageSize(pageParams.getPageSize());


        return result;
    }


    @Override
    public CourseBaseInfoDto createCourseBase(AddCourseDto dto, Long companyId) {
        // 1.对参数进行合法性的校验 --- 交给JSR-303 进行参数校验
//        if (StringUtils.isBlank(dto.getName())) {
//            throw new XueChengPlusException("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new XueChengPlusException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new XueChengPlusException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new XueChengPlusException("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new XueChengPlusException("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new XueChengPlusException("适应人群");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new XueChengPlusException("收费规则为空");
//        }

        // 2.对数据进行封装，调用mapper进行数据持久化

        //2.1课程基本信息
        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto,courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);


        //2.2课程营销信息
        //先根据课程id查询营销信息
        Long courseId = courseBaseNew.getId();
        CourseMarket courseMarketNew = courseMarketMapper.selectById(courseId);
        if(courseMarketNew != null){
//            throw new RuntimeException("不允许重复添加课程！！！");
            throw new XueChengPlusException("不允许重复添加课程！！！");
        }
        courseMarketNew =  new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarketNew);
        courseMarketNew.setId(courseId);
        //收费规则
        String charge = dto.getCharge();

        //收费课程必须写价格
        if ("201001".equals(charge)) {
            Float price = dto.getPrice();
            if (ObjectUtils.isEmpty(price) || price <= 0) {
//                throw new RuntimeException("收费课程价格不能为空");
                throw new XueChengPlusException("收费课程价格不规范");
            }
            courseMarketNew.setPrice(dto.getPrice());
            courseMarketNew.setOriginalPrice(dto.getOriginalPrice());
        }

        //插入课程营销信息
        int insert1 = courseMarketMapper.insert(courseMarketNew);

        if(insert<=0 || insert1<=0){
            throw new XueChengPlusException("新增课程基本信息失败！！！");
        }


        //添加成功
        //返回添加的课程信息给前端
        return getCourseBaseInfo(courseId);

    }


    /**
     *  封装 课程的基本信息 和 营销信息
     * @param courseId  课程id
     * @return 课程全部信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        // 课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 信息组装
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        // 根据课程分类的id查询分类的名称  查询策略表
        String mt = courseBase.getMt();
        CourseCategory mtCategory = courseCategoryMapper.selectById(mt);
        if (mtCategory != null) {
            courseBaseInfoDto.setMtName(mtCategory.getName());
        }
        String st = courseBase.getSt();
        CourseCategory stCategory = courseCategoryMapper.selectById(st);
        if (stCategory != null) {
            courseBaseInfoDto.setStName(stCategory.getName());
        }

        return courseBaseInfoDto;
    }
}




