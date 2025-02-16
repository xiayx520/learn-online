package com.xia.content.service;

import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.model.dto.QueryCourseParamsDto;
import com.xia.content.model.po.CourseBase;

public interface CourseBaseInfoService {
    /**
     * 课程查询接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
