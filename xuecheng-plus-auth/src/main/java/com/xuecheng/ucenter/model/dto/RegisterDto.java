package com.xuecheng.ucenter.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName RegisterDto
 * @Date 2023/2/12 10:31
 * @Author diane
 * @Description 用户注册 dto
 * @Version 1.0
 */
@Data
@ApiModel(value="RegisterDto", description="注册基本信息")
public class RegisterDto extends CheckCodePasDto {

    @NotEmpty(message = "手机号不能为空")
    private String cellphone;

    @NotEmpty(message = "账号不能为空")
    private String username;

    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @NotEmpty(message = "昵称不能为空")
    private String nickname;

}
