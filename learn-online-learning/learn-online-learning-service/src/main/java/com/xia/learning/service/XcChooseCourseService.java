package com.xia.learning.service;

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
}
