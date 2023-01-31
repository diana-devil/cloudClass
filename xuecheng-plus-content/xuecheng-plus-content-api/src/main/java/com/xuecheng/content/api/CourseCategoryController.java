package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName CourseCategoryController
 * @Date 2023/1/31 15:01
 * @Author diane
 * @Description TODO
 * @Version 1.0
 */
@RestController
@Api(value = "课程分类相关接口", tags = "课程分类相关接口")
@Slf4j
public class CourseCategoryController {

    @Resource
    private CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类查询接口")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes();
    }
}
