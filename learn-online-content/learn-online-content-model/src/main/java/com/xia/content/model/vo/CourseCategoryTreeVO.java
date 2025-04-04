package com.xia.content.model.vo;

import com.xia.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * 课程分类树形结构
 */
@Data
public class CourseCategoryTreeVO extends CourseCategory {

    private List<CourseCategoryTreeVO> childrenTreeNodes;

}
