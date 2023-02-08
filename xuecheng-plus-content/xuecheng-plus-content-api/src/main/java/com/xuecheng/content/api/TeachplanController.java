package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanMediaService;
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

    @Resource
    private TeachplanMediaService teachplanMediaService;

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

    @ApiOperation("删除课程计划")
    @DeleteMapping("/{id}")
    public void removeTeachPlan(@PathVariable Long id) {
        teachplanService.removeTeachPlan(id);
    }


    @ApiOperation("移动课程计划")
    @PostMapping("/{moveType}/{id}")
    public void moveTeachPlan(@PathVariable String moveType, @PathVariable Long id) {
        teachplanService.moveTeachPlan(moveType, id);
    }



    @ApiOperation("教学计划绑定媒资信息")
    @PostMapping("/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanMediaService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("删除教学计划绑定的媒资信息")
    @DeleteMapping("/association/media/{teachplanId}/{mediaId}")
    public void removeAssociationMedia(@PathVariable Long teachplanId, @PathVariable String mediaId) {
        teachplanMediaService.removeAssociationMedia(teachplanId, mediaId);
    }





}
