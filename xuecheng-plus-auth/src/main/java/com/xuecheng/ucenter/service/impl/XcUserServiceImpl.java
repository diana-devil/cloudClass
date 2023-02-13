package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.feignClient.CheckCodeClient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.CheckCodePasDto;
import com.xuecheng.ucenter.model.dto.FindPassDto;
import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.IXcUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @ClassName XcUserServiceImpl
 * @Date 2023/2/12 10:37
 * @Author diane
 * @Description XcUser 实现类
 * @Version 1.0
 */
@Service
@Slf4j
public class XcUserServiceImpl extends ServiceImpl<XcUserMapper, XcUser> implements IXcUserService {

    @Resource
    private CheckCodeClient checkCodeClient;

    @Resource
    private XcUserServiceImpl currentProxy;

    @Resource
    private XcUserRoleMapper xcUserRoleMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * @param registerDto 注册信息
     */
    @Override
    public void register(RegisterDto registerDto) {
        // 校验 验证码 和 密码
        checkout(registerDto);

        // 检测用户信息是否存在
        XcUser xcUser = getOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, registerDto.getUsername()));
        if (xcUser != null) {
            XueChengPlusException.exce("该用户名已经存在,请换一个吧！");
        }
        // 封装用户数据，存入数据库
        xcUser = new XcUser();
        BeanUtils.copyProperties(registerDto, xcUser);
        String userId = String.valueOf(UUID.randomUUID());
        xcUser.setId(userId);
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        xcUser.setName("学生");
        // 将密码加密
        String password = xcUser.getPassword();
        String passwordDb = passwordEncoder.encode(password);
        xcUser.setPassword(passwordDb);
        currentProxy.addXcUserToDb(xcUser);
        log.info("注册成功！");
    }

    /**
     * 校验 密码和验证码
     * 参数 为 公共父类
     * @param checkCodePasDto  验证类 dto
     */
    private void checkout(CheckCodePasDto checkCodePasDto) {
        // 校验验证码
        Boolean verifyCheckCode = checkCodeClient.verify(checkCodePasDto.getCheckcodekey(), checkCodePasDto.getCheckcode());
        if (verifyCheckCode == null) {
            log.info("注册时，调用验证码远程验证服务失败！");
            XueChengPlusException.exce("网络延迟，请稍后再试！");
        }
        if (!verifyCheckCode) {
            XueChengPlusException.exce("验证码不正确！");
        }
        // 校验 二次密码输入
        String password = checkCodePasDto.getPassword();
        String confirmpwd = checkCodePasDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            XueChengPlusException.exce("两次密码输入不一致");
        }
    }


    /**
     * 将用户信息和用户关系角色信息 保存到数据库
     * @param xcUser 用户信息
     */
    @Transactional
    public void addXcUserToDb(XcUser xcUser) {
        System.out.println(xcUser);
        // 将学生信息 保存至用户表
        boolean save = save(xcUser);
        if (!save) {
            XueChengPlusException.exce("保存用户信息异常！");
        }
        // 更新 用户角色关系表
        System.out.println(xcUser);
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(xcUser.getId());
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        int insert = xcUserRoleMapper.insert(xcUserRole);
        if (insert <= 0) {
            XueChengPlusException.exce("更新用户关系表失败！");
        }
    }


    /**
     * 找回密码
     * @param findPassDto 找回密码信息
     */
    @Override
    public void findPassword(FindPassDto findPassDto) {
        // 校验密码和验证码
        checkout(findPassDto);
        // 根据 手机号和邮箱 查询用户
        String cellphone = findPassDto.getCellphone();
        String email = findPassDto.getEmail();
        LambdaQueryWrapper<XcUser> query = new LambdaQueryWrapper<>();
        query.eq(StringUtils.isNotBlank(cellphone), XcUser::getCellphone, cellphone);
        query.eq(StringUtils.isNotBlank(email), XcUser::getEmail, email);
        XcUser xcUser = getOne(query);
        if (xcUser == null) {
            XueChengPlusException.exce("用户不存在！");
        }

        // 将密码加密
        String password = findPassDto.getPassword();
        String passwordDb = passwordEncoder.encode(password);
        xcUser.setPassword(passwordDb);
        // 更新用户的 密码
        boolean b = updateById(xcUser);
        if (!b) {
            XueChengPlusException.exce("更新密码失败！");
        }
    }


}
