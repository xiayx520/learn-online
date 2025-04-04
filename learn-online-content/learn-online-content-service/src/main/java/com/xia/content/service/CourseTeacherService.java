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

    /**
     * 添加或修改课程教师信息
     * @param courseTeacher
     * @param companyId
     * @return
     */
    CourseTeacher addOrUpdateCourseTeacher(CourseTeacher courseTeacher, Long companyId);

    /**
     * 根据课程id和教师id删除课程教师信息
     * @param courseId
     * @param teacherId
     * @param companyId
     */
    void deleteCourseTeacher(Long courseId, Long teacherId, Long companyId);
}
