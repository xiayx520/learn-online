package com.xia.content.service;

import com.xia.content.model.vo.CourseCategoryTreeVO;

import java.util.List;

public interface CourseCategoryService {

    /**
     * 查询课程分类
     * @param parentId 课程父级id
     * @return 课程分类
     */
    List<CourseCategoryTreeVO> queryTreeNodes(String parentId);
}
