package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.feignClient.CheckCodeClient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName PasswordAuthServiceImpl
 * @Date 2023/2/11 15:01
 * @Author diane
 * @Description  账号密码认证
 * @Version 1.0
 */
@Slf4j
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    /**
     * 加密的对象
     */
    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private CheckCodeClient checkCodeClient;


    /**
     *  根据用户输入的 用户名 和 密码 及 验证码 ，完成校验工作
     * @param authParamsDto 认证参数
     * @return 认证信息
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        log.info("用户名-密码方式校验");
        System.out.println(authParamsDto);

        // 0.校验验证码
        // String checkcode = authParamsDto.getCheckcode();
        // String checkcodekey = authParamsDto.getCheckcodekey();
        // if(StringUtils.isBlank(checkcodekey) || StringUtils.isBlank(checkcode)){
        //     XueChengPlusException.exce("验证码为空!");
        // }
        // Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        // if (verify == null) {
        //     XueChengPlusException.exce("验证码服务远程调用失败!");
        // }
        // if (!verify) {
        //     XueChengPlusException.exce("验证码校验失败！");
        // }


        // 1.获取用户名 和 密码，并进行空值校验
        String username = authParamsDto.getUsername();
        if (StringUtils.isBlank(username)) {
            XueChengPlusException.exce("请输入用户名！");
        }
        // 用户输入密码
        String password = authParamsDto.getPassword();
        if (StringUtils.isBlank(password)) {
            XueChengPlusException.exce("请输入密码！");
        }

        // 2.通过用户名 从数据库查询数据
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) {
            // DaoAuthenticationProvider 调用该方法loadUserByUsername时，当返回为null时，抛出异常。
            // return null;
            // 自己完成校验工作，则需要抛出异常
            XueChengPlusException.exce("用户不存在！");
        }

        // 3.比对密码，进行密码校验
        // 数据库查询密码
        String passwordDb = xcUser.getPassword();
        // 密码校验
        boolean matches = passwordEncoder.matches(password, passwordDb);
        if (!matches) {
            XueChengPlusException.exce("用户名或密码错误！");
        }
        // 4. 认证完成，封装返回信息
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        log.info("用户名-密码 认证通过！");
        return xcUserExt;

    }
}
