package com.xia.content.service.impl;

import com.xia.base.exception.GlobalException;
import com.xia.content.mapper.CoursePublishMapper;
import com.xia.content.model.po.CourseTeacher;
import com.xia.content.model.vo.CourseBaseInfoVO;
import com.xia.content.model.vo.CoursePreviewVO;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.CourseBaseInfoService;
import com.xia.content.service.CoursePublishService;
import com.xia.content.service.CourseTeacherService;
import com.xia.content.service.TeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CoursePublishServiceImpl implements CoursePublishService {


    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private CourseTeacherService courseTeacherService;

    @Autowired
    private TeachPlanService teachPlanService;

    /**
     * 根据课程id查询课程发布信息
     * @param courseId
     * @return
     */
    @Override
    public CoursePreviewVO getCoursePreviewVO(Long courseId) {
        if(courseId == null) {
            throw new GlobalException("课程id为空");
        }
        CourseBaseInfoVO courseBaseInfoVO = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachPlanVO> teachPlanTreeNodes = teachPlanService.getTeachPlanTree(courseId);
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacher(courseId);

        CoursePreviewVO coursePreviewVO = new CoursePreviewVO();
        coursePreviewVO.setCourseBase(courseBaseInfoVO);
        coursePreviewVO.setTeachplans(teachPlanTreeNodes);
        coursePreviewVO.setCourseTeacher(courseTeachers);

        return coursePreviewVO;
    }
}
