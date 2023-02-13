package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.service.IXcUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName RegisterController
 * @Date 2023/2/12 12:09
 * @Author diane
 * @Description TODO
 * @Version 1.0
 */
@Slf4j
@RestController
@Api(value = "注册",tags = "注册接口")
public class RegisterController {

    @Autowired
    private IXcUserService xcUserService;

    @RequestMapping("/register")
    @ApiOperation("用户注册")
    public void register(@RequestBody @Validated RegisterDto registerDto) {
       xcUserService.register(registerDto);
    }
}
