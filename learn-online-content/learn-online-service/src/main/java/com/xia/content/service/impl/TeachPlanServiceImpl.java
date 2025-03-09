package com.xia.content.service.impl;

import com.xia.content.mapper.TeachplanMapper;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.TeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    /**
     * 获取课程计划
     * @param courseId 课程id
     * @return 课程计划
     */
    @Override
    public List<TeachPlanVO> getTeachPlanTree(Long courseId) {
        return teachplanMapper.getTeachPlanTree(courseId);
    }
}
