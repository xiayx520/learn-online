package com.xia.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xia.content.model.po.CourseCategory;
import com.xia.content.model.vo.CourseCategoryTreeVO;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /**
     * 根据父结点id查询子结点
     * @param parentId
     * @return
     */
    List<CourseCategoryTreeVO> queryTreeNodes(String parentId);
}
