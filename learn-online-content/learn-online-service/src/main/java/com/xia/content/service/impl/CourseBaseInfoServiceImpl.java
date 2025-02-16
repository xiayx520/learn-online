package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.mapper.CourseBaseMapper;
import com.xia.content.model.dto.QueryCourseParamsDto;
import com.xia.content.model.po.CourseBase;
import com.xia.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 根据查询条件分页查询课程
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        //使用lambda表达式构建QueryWrapper
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNoneEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNoneEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus())
                .eq(StringUtils.isNoneEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        //构造分页查询条件
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        return new PageResult<CourseBase>(courseBasePage.getRecords(),courseBasePage.getTotal(),pageParams.getPageNo(),pageParams.getPageSize());
    }
}
