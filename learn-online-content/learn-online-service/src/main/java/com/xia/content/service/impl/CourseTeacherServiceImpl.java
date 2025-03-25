package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xia.content.mapper.CourseBaseMapper;
import com.xia.content.mapper.CourseTeacherMapper;
import com.xia.content.model.po.CourseTeacher;
import com.xia.content.service.CourseTeacherService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;

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

    /**
     * 添加或修改课程教师信息
     * @param courseTeacher
     * @param companyId
     * @return
     */
    @Override
    public CourseTeacher addOrUpdateCourseTeacher(CourseTeacher courseTeacher, Long companyId) {
        // 根据课程id查询课程信息,只允许向机构自己的课程中添加老师
        if (courseBaseMapper.selectById(courseTeacher.getCourseId()).getCompanyId().equals(companyId)) {
            if(StringUtils.isBlank(courseTeacher.getTeacherName()) || StringUtils.isBlank(courseTeacher.getPosition())) {
                throw new RuntimeException("教师姓名或职位不能为空");
            }
            if(courseTeacher.getId() == null){
                // 添加课程教师信息
                courseTeacher.setCreateDate(LocalDateTime.now());
                courseTeacherMapper.insert(courseTeacher);
                return courseTeacher;
            }else {
                // 修改课程教师信息
                courseTeacherMapper.updateById(courseTeacher);
                return courseTeacher;
            }
        }else {
            throw new RuntimeException("没有权限修改该不属于您的课程");
        }
    }
}
