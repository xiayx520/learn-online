package com.xia.content.service;

import com.xia.content.model.vo.CoursePreviewVO;

public interface CoursePublishService {

    /**
     * 根据课程id查询课程发布信息
     * @param courseId
     * @return
     */
    CoursePreviewVO getCoursePreviewVO(Long courseId);

    /**
     * 课程提交审核
     * @param courseId
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    void publishCourse(Long companyId, Long courseId);
}
