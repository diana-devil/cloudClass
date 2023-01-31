package com.xuecheng.content.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName teachplan_work
 */
@TableName(value ="teachplan_work")
@Data
public class TeachplanWork implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作业信息标识
     */
    private Long workId;

    /**
     * 作业标题
     */
    private String workTitle;

    /**
     * 课程计划标识
     */
    private Long teachplanId;

    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 
     */
    private Date createDate;

    /**
     * 
     */
    private Long coursePubId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}