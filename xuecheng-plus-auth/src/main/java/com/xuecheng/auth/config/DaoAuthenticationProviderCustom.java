package com.xuecheng.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @ClassName DaoAuthenticationProviderCustom
 * @Date 2023/2/11 12:07
 * @Author diane
 * @Description 自定义类 继承 DaoAuthenticationProvider
 *      通过重写里面的方法 additionalAuthenticationChecks()方法
 *      来实现多种方式 认证登陆
 * @Version 1.0
 */
@Component
@Slf4j
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {

    /**
     *  注入  UserDetailsService 对象
     * @param userDetailsService 从容器拿的，是自己定义的那个 UserServiceImpl ，实现了 接口 UserDetailsService
     */
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }


    /**
     *  置空 密码判断
     * @param userDetails
     * @param authentication
     */
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)  {


    }

}
