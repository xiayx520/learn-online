package com.xia.content.service;

import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.model.dto.AddCourseDto;
import com.xia.content.model.dto.EditCourseDto;
import com.xia.content.model.dto.QueryCourseParamsDto;
import com.xia.content.model.po.CourseBase;
import com.xia.content.model.vo.CourseBaseInfoVO;

public interface CourseBaseInfoService {
    /**
     * 课程查询接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程基本信息
     * @param addCourseDto
     * @return
     */
    CourseBaseInfoVO createCourseBase(AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程基本信息
     * @param courseId
     * @return
     */
    CourseBaseInfoVO getCourseBaseInfo(Long courseId);

    /**
     * 修改课程基本信息
     * @param companyId
     * @param editCourseDto
     * @return
     */
    CourseBaseInfoVO updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 删除课程基本信息
     * @param courseId
     * @param companyId
     */
    void deleteCourseBase(Long courseId, Long companyId);
}
