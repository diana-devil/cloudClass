package com.xuecheng.ucenter.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName LoginParamsDto
 * @Date 2023/2/12 15:39
 * @Author diane
 * @Description 密码 和 验证码 校验类
 * @Version 1.0
 */
@Data
public class CheckCodePasDto {

    @NotEmpty(message = "密码不能为空")
    private String password;

    /**
     * 确认密码
     */
    @NotEmpty(message = "确认密码不能为空")
    private String confirmpwd;

    /**
     * 验证码
     */
    @NotEmpty(message = "验证码不能为空")
    private String checkcode;

    /**
     * 验证码的key
     */
    @NotEmpty(message = "验证码 key 不能为空")
    private String checkcodekey;

}
