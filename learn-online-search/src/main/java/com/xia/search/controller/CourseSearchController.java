package com.xia.search.controller;


import com.xia.base.model.PageParams;
import com.xia.search.dto.SearchCourseParamDto;
import com.xia.search.vo.SearchPageResultVO;
import com.xia.search.po.CourseIndex;
import com.xia.search.service.CourseSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程搜索接口
 * @date 2022/9/24 22:31
 */
@Api(value = "课程搜索接口", tags = "课程搜索接口")
@RestController
@RequestMapping("/course")
public class CourseSearchController {

    @Autowired
    CourseSearchService courseSearchService;


    @ApiOperation("课程搜索列表")
    @GetMapping("/list")
    public SearchPageResultVO<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto) {

        return courseSearchService.queryCoursePubIndex(pageParams, searchCourseParamDto);

    }
}
