package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @ClassName AuthService
 * @Date 2023/2/11 14:56
 * @Author diane
 * @Description 认证接口
 *      一个接口,定义方法-execute
 * @Version 1.0
 */
public interface AuthService {

    /**
     * @description 统一认证接口 调用的 的 认证方法
     * @param authParamsDto 认证参数
     * @return 用户认证，权限信息
     * @author Mr.M
     * @date 2022/9/29 12:11
     */
    XcUserExt execute(AuthParamsDto authParamsDto);

}
