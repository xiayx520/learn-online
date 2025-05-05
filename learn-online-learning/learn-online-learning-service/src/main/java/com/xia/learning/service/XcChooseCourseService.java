package com.xia.learning.service;

import com.xia.base.model.PageResult;
import com.xia.learning.model.dto.MyCourseTableParams;
import com.xia.learning.model.po.XcChooseCourse;
import com.xia.learning.model.po.XcCourseTables;
import com.xia.learning.model.vo.XcChooseCourseVO;
import com.xia.learning.model.vo.XcCourseTablesVO;

public interface XcChooseCourseService {
    /**
     * 添加选课
     * @param courseId
     * @param userId
     * @return
     */
    XcChooseCourseVO addChooseCourse(Long courseId, String userId);


    /**
     * 查询学习资格
     * @param userId
     * @param courseId
     * @return
     */
    XcCourseTablesVO getLearningStatus(String userId, Long courseId);


    /**
     * 添加我的课表
     * @param xcChooseCourse
     * @return
     */
    XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse);

    /**
     * 我的课程表
     * @param params
     * @return
     */
    PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params);
}
