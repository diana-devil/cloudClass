package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;


/**
* @author 凉冰
* @description 针对表【course_category(课程分类)】的数据库操作Service
* @createDate 2023-01-31 09:55:38
*/
public interface CourseCategoryService extends IService<CourseCategory> {

    /**
     * 课程分类树形结构查询
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes();

}
