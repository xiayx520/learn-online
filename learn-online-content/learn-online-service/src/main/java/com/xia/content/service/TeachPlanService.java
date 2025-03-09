package com.xia.content.service;

import com.xia.content.model.vo.TeachPlanVO;

import java.util.List;

public interface TeachPlanService {

    /**
     * 根据课程id查询课程计划
     * @param courseId 课程id
     * @return 课程计划
     */
    List<TeachPlanVO> getTeachPlanTree(Long courseId);
}
