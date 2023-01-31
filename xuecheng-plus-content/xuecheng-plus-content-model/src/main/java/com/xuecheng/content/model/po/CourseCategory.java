package com.xuecheng.content.model.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 课程分类
 * @TableName course_category
 */
@TableName(value ="course_category")
@Data
public class CourseCategory implements Serializable {
    /**
     * 主键
     */
    @TableId
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类标签默认和名称一样
     */
    private String label;

    /**
     * 父结点id（第一级的父节点是0，自关联字段id）
     */
    private String parentid;

    /**
     * 是否显示
     */
    private Integer isShow;

    /**
     * 排序字段
     */
    private Integer orderby;

    /**
     * 是否叶子
     */
    private Integer isLeaf;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}