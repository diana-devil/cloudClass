package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Mr.M
 * @version 1.0
 * @description 安全管理配置
 * @date 2022/9/26 20:53
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    /**
     * 注入 自定义的类 继承自 DaoAuthenticationProvider
     */
    @Autowired
    DaoAuthenticationProviderCustom daoAuthenticationProviderCustom;

    /**
     * 使用自定义的类 来代替 框架的DaoAuthenticationProvider
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProviderCustom);
    }



    /**
     * 配置认证管理bean
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }



    //配置用户信息服务
    // 自己手动实现 UserDetailsService 这个接口, 实现从数据库查询用户数据
    //@Bean
    //public UserDetailsService userDetailsService() {
    //    //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
    //    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    //    // 内存中创建了两个账户, 并分配了权限
    //    manager.createUser(User.withUsername("zhangsan").password("123").authorities("p1").build());
    //    manager.createUser(User.withUsername("lisi").password("456").authorities("p2").build());
    //    return manager;
    //}


    @Bean
    public PasswordEncoder passwordEncoder() {
       // 密码为明文方式
       // return NoOpPasswordEncoder.getInstance();
        // 密码加密方式 -- 影响 （用户密码 + 客户端密钥）
        // 用户输入的时候 输入正常的 密码和秘钥； 但是客户端在存储的时候，要存加密后的数据
        return new BCryptPasswordEncoder();
    }

    //配置安全拦截机制
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 忽略 注册接口 -- 允许其使用post接口
        // key 默认是不让 使用POST请求的, 这里需要 忽略这个 注册这个 接口
        http.csrf().ignoringAntMatchers("/register", "/findpassword");



        http
                .authorizeRequests()
                .antMatchers("/r/**").authenticated()//访问/r开始的请求需要认证通过
                .anyRequest().permitAll()//其它请求全部放行
                .and()
                .formLogin().successForwardUrl("/login-success");//登录成功跳转到/login-success

        // 设置退出地址
        http.logout().logoutUrl("/logout");//退出地址
    }


    // 测试加密
    public static void main(String[] args) {
        String password = "123456";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        for(int i=0;i<10;i++) {
            //每个计算出的Hash值都不一样
            String hashPass = passwordEncoder.encode(password);
            System.out.println(hashPass);
            //虽然每次计算的密码Hash值不一样但是校验是通过的
            boolean f = passwordEncoder.matches(password, hashPass);
            System.out.println(f);
        }
    }




}
