package com.xia.content.service;

import com.xia.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {

    /**
     * 根据课程id查询课程教师信息
     * @param id
     * @return
     */
    List<CourseTeacher> getCourseTeacher(Long id);
}
