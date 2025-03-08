package com.xia.content.api;

import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.model.dto.AddCourseDto;
import com.xia.content.model.dto.EditCourseDto;
import com.xia.content.model.dto.QueryCourseParamsDto;
import com.xia.content.model.po.CourseBase;
import com.xia.content.model.vo.CourseBaseInfoVO;
import com.xia.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "课程信息管理")
@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    /**
     * 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return
     */
    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto)
    {
        log.info("查询课程列表，参数：{}",queryCourseParamsDto);
        return courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParamsDto);
    }


    /**
     * 新增课程基本信息
     * @param addCourseDto 新增课程基本信息
     * @return 新增课程基本信息
     */
    @ApiOperation("新增课程基本信息")
    @PostMapping("/course")
    public CourseBaseInfoVO createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto)
    {
        log.info("新增课程，参数：{}",addCourseDto);
        return courseBaseInfoService.createCourseBase(addCourseDto);
    }


    /**
     * 根据课程id查询课程基本信息
     * @param courseId 课程id
     * @return 课程基本信息
     */
    @ApiOperation("根据课程id查询课程基本信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoVO getCourseBaseById(@PathVariable Long courseId)
    {
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }


    /**
     * 修改课程基本信息
     * @param editCourseDto 修改课程基本信息
     * @return 修改课程基本信息
     */
    @ApiOperation("修改课程基本信息")
    @PutMapping("/course")
    public CourseBaseInfoVO editCourseBase(@RequestBody @Validated EditCourseDto editCourseDto)
    {
        //todo 机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

}
