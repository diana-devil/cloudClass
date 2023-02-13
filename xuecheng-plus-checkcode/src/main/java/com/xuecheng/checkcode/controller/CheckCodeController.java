package com.xuecheng.checkcode.controller;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.CheckCodeService;
import com.xuecheng.checkcode.service.impl.CheckCodeServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Mr.M
 * @version 1.0
 * @description 验证码服务接口
 * @date 2022/9/29 18:39
 */
@Api(value = "验证码服务接口")
@RestController
@Slf4j
public class CheckCodeController {

    @Resource
    private CheckCodeServiceImpl checkCodeService;

    @Resource(name = "PicCheckCodeService")
    private CheckCodeService picCheckCodeService;


    @ApiOperation(value="生成验证信息", notes="生成验证信息")
    @PostMapping(value = "/pic")
    public CheckCodeResultDto generatePicCheckCode(CheckCodeParamsDto checkCodeParamsDto){
        return picCheckCodeService.generate(checkCodeParamsDto);
    }

    @ApiOperation(value="校验", notes="校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "业务名称", required = true, dataType = "String", paramType="query"),
            @ApiImplicitParam(name = "key", value = "验证key", required = true, dataType = "String", paramType="query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String", paramType="query")
    })
    @PostMapping(value = "/verify")
    public Boolean verify(@RequestParam("key_code") String key_code, @RequestParam("code") String code){
        Boolean isSuccess = picCheckCodeService.verify(key_code,code);
        return isSuccess;
    }

    /**
     * 根据传入的手机号或者邮箱地址 获取验证码
     * @param param1 手机号或者邮箱地址
     * @return 验证码
     */
    @ApiOperation(value="获取验证码", notes="获取验证码")
    @PostMapping("/phone")
    public CheckCodeResultDto getCheckCode(@RequestParam("param1") String param1) {
        CheckCodeParamsDto checkCodeParamsDto = new CheckCodeParamsDto();
        checkCodeParamsDto.setParam1(param1);
        return checkCodeService.generate(checkCodeParamsDto);
    }





}
