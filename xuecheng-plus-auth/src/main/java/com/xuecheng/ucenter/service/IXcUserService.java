package com.xuecheng.ucenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.ucenter.model.dto.FindPassDto;
import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @ClassName IXcUserService
 * @Date 2023/2/12 10:37
 * @Author diane
 * @Description TODO
 * @Version 1.0
 */
public interface IXcUserService extends IService<XcUser>{
    /**
     * 用户注册
     * @param registerDto 注册信息
     */
    void register(RegisterDto registerDto);

    /**
     * 找回密码
     * @param findPassDto 找回密码信息
     */
    void findPassword(FindPassDto findPassDto);
}
