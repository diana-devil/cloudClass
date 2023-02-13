package com.xuecheng.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("xc_user")
public class XcUser implements Serializable {

    private static final long serialVersionUID = 1L;


    private String id;

    private String username;

    private String password;

    private String salt;

    private String name;
    private String nickname;
    private String wxUnionid;
    private Long companyId;
    /**
     * 头像
     */
    private String userpic;

    private String utype;

    private LocalDateTime birthday;

    private String sex;

    private String email;

    private String cellphone;

    private String qq;

    /**
     * 逻辑删除——无需写在配置文件中
     * 状态（1正常  0删除）
     */
    @TableLogic(value = "1", delval = "0")//默认是1 1表示未删除，0表示删除
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
