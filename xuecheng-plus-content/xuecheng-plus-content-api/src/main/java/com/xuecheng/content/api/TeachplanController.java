package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName TeachplanController
 * @Date 2023/2/4 20:03
 * @Author diane
 * @Description 课程计划编辑接口
 * @Version 1.0
 */
@RequestMapping("/teachplan")
@RestController
// 用在请求的类上，表示对类的说明
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
public class TeachplanController {

    @Resource
    private TeachplanService teachplanService;

    @GetMapping("/{courseId}/tree-nodes")
    //用在请求的方法上，说明方法的用途、作用
    @ApiOperation("查询课程计划树形结构")
    // 用在请求的方法上，表示一组参数说明
    @ApiImplicitParam(value = "courseId",name = "课程基础Id值",required = true,dataType = "Long",paramType = "path")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplayTree(courseId);
    }


    @ApiOperation("新增/修改课程计划")
    @PostMapping
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto) {
        teachplanService.saveTeachplan(saveTeachplanDto);

    }

}
