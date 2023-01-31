package com.xuecheng;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ContentServiceApplication.class)
class ContentServiceApplicationTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseBaseService courseBaseService;

    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(1L);
        Assertions.assertNotNull(courseBase);
        System.out.println(courseBase);
    }

    @Test
    void testCourseBaseService() {
        PageParams pageParams = new PageParams(1, 10);
        QueryCourseParamsDto queryCourseParams = new QueryCourseParamsDto();
        PageResult<CourseBase> result = courseBaseService.queryCourseBaseList(pageParams, queryCourseParams);
        Assertions.assertNotNull(result);
        System.out.println(result);
    }

}
