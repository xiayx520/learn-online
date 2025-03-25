package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xia.content.mapper.CourseTeacherMapper;
import com.xia.content.model.po.CourseTeacher;
import com.xia.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    /**
     * 根据课程id查询课程教师信息
     * @param id
     * @return
     */
    @Override
    public List<CourseTeacher> getCourseTeacher(Long id) {
        // 根据课程id查询课程教师信息
        return courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, id)
        );

    }
}
