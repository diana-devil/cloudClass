package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @ClassName WxLoginController
 * @Date 2023/2/11 19:38
 * @Author diane
 * @Description 微信扫描登陆接口
 * @Version 1.0
 */
@Slf4j
@Controller
public class WxLoginController {

    @Resource
    private WxAuthServiceImpl wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        XcUser xcUser = wxAuthService.wxAuth(code);
        // 如果没有拿到用户信息，就重定向到错误页面
        if(xcUser == null){
            return "redirect:http://www.xuecheng-plus.com/error.html";
        }
        // 拿到用户信息，重定向到登陆页面
        String username = xcUser.getUsername();
        return "redirect:http://www.xuecheng-plus.com/sign.html?username="+username+"&authType=wx";
    }

}
