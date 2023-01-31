package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName CourseCategoryTreeDto
 * @Date 2023/1/31 15:02
 * @Author diane
 * @Description 树形数据类
 * @Version 1.0
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    /**
     * 子结点
     */
    private List<CourseCategory> childrenTreeNodes;

    // 重写 toString 方法
    @Override
    public String toString() {
        return "CourseCategory{" +
                "id='" + super.getId() + '\'' +
                ", name='" + super.getName() + '\'' +
                ", label='" + super.getLabel() + '\'' +
                ", parentid='" + super.getParentid() + '\'' +
                ", isShow=" + super.getIsShow() +
                ", orderby=" + super.getOrderby() +
                ", isLeaf=" + super.getIsLeaf() +
                ", childrenTreeNodes=" + childrenTreeNodes +
                '}';
    }





}
