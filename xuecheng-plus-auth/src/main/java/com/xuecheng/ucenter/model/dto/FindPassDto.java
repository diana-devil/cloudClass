package com.xuecheng.ucenter.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @ClassName findPassDto
 * @Date 2023/2/12 15:33
 * @Author diane
 * @Description 找回密码 dto
 * @Version 1.0
 */
@Data
@ApiModel(value="FindPassDto", description="注册基本信息")
public class FindPassDto extends CheckCodePasDto {

    private String cellphone;

    private String email;

}
