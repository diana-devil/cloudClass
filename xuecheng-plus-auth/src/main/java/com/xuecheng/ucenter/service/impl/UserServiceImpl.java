package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName UserServiceImpl
 * @Date 2023/2/11 10:26
 * @Author diane
 * @Description  完成校验后，封装用户信息到username中，返回给框架
 *
 * 实现UserDetailsService 接口的 loadUserByUsername(String s) 方法
 *          一般 由 DaoAuthenticationProvider调用的 loadUserByUsername() 方法只能校验密码，实现用户名密码的登陆方式
 *          如果我们要想实现多种登入方式，就必须 将 loadUserByUsername 写成一个统一认证的入口
 *              1.loadUserByUsername方法传参数s ,不是单纯的用户名；而是封装了各种登陆信息的Dto类 -- AuthParamsDto
 *                  1.1 该dto类型，必须包含 一个参数:authType -- 认证类型
 *                  1.2 方法内解析参数,根据 认证类型 进行不同类型的认证
 *                  1.3 自定义接口AuthService-方法execute,交由不同的实现类实现，来完成不同类型的认证
 *                      -- key 设计模式 ------- 策略模式
 *                      -- 定义抽象策略类--AuthService,并定义校验方法-execute()
 *                      -- 定义具体策略类-- PasswordAuthServiceImpl,……, 实现校验方法
 *                      -- 由上下文对象，通过不同的认证类型，调用不同的bean(调用不同策略类)，执行不同的校验方法
 *
 *              2.重写 DaoAuthenticationProvider 密码校验方法
 *                  3.1 自定义类 DaoAuthenticationProviderCustom  继承 DaoAuthenticationProvider
 *                  3.2 在 WebSecurityConfig类中 指定 自定义类 来代替 DaoAuthenticationProvider
 *                  3.3 将 其内部方法-additionalAuthenticationChecks()方法 置空
 *                      3.3.1 默认是直接校验 密码，只支持 用户名-密码方式登陆
 *                      3.3.2 置空后, 框架不进行密码比较；自己实现认证校验-接口AuthService-方法execute
 *
 * @Version 1.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private XcMenuMapper xcMenuMapper;

    /**
     * 实现  统一认证的入口
     *      通过认证bean的方式，根据不同类型，执行不同的方法
     * @param s 包含-校验类型的-json串
     * @return UserDetails 对象
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        System.out.println(s);

        // 1.将参数 转换成  AuthParamsDto类型
        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        // 2.根据不同的认证类型，进行认证
        // 得到认证类型
        String authType = authParamsDto.getAuthType();
        // 从上下文对象中获取指定认证类型的bean
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        // 执行方法
        XcUserExt xcUser = authService.execute(authParamsDto);


        // 3.给框架返回 UserDetails 对象
        return getUserPrincipal(xcUser);

    }

    /**
     * 先查询用户权限
     * 将查询用户信息 + 用户权限 封装到令牌, 并返回UserDetails对象
     *
     *      1.key 扩充用户信息-两种方法
     *        前提：jwt令牌默认只存储 username，因为UserDetails中只定义了用户名和密码
     *         1.1扩充userName,用json串存储数据，将大部分用户信息存到json串中
     *         1.2自定义类，扩展 UserDetails ，这样令牌就能返回更多信息
     *
     *      2.为什么可以扩充userName,不影响校验嘛?
     *          2.1 原始校验密码方式
         *          经过测试，在校验的时候，只校验密码，不会校验用户名
         *          用户名只是由我们来去查询数据库，获取密码的。
         *          所以我们可以先由正常的userName去查询数据库，获取密码，然后在返回数据的时候，用包含用户信息的json串修改userName
     *          2.2 自定义密码校验方式
     *              因为密码校验前面已经完成了，不影响后序校验
     * @param xcUser 用户信息
     * @return
     */
    private UserDetails getUserPrincipal(XcUserExt xcUser) {


        // 从数据库中获取完成的XcMenu的list集合
        List<XcMenu> xcMenuList = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        // 从集合中提取出 code字段
        List<String> authoritieList = xcMenuList.stream().map(XcMenu::getCode).collect(Collectors.toList());
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection;所以给他一个空的权限
        String[] authorities = {"test"};
        if (authoritieList.size() > 0) {
            // 将list集合 转换成String数组  权限是要用 string[] 的方式传递的
            authorities = authoritieList.toArray(new String[0]);
        }


        // 通过扩充userName的方式-扩充用户信息
        xcUser.setPassword(null);
        xcUser.setPermissions(authoritieList);
        String str = JSON.toJSONString(xcUser);

        // 用户名，密码，权限 至少这三个
        // 这里用 包含用户信息的json串 来代替用户名，以方面我们在jwt令牌中 获取更多的用户信息
        // 密码置空即可，因为认证校验工作已经在前面完成，且DaoAuthenticationProvider的密码校验工作已经置空
        UserDetails userDetails = User.withUsername(str).password("").authorities(authorities).build();
        return userDetails;

    }
}
