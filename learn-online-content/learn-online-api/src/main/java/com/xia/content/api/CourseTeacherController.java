package com.xia.content.api;

import com.xia.content.model.po.CourseTeacher;
import com.xia.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "课程教师管理")
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;


    /**
     * 根据课程id查询课程教师信息
     * @param id
     * @return
     */

    @GetMapping("/courseTeacher/list/{id}")
    @ApiOperation("根据课程id查询课程教师信息")
    public List<CourseTeacher> getCourseTeacher(@PathVariable Long id) {
        log.info("根据课程id查询课程教师信息，参数：{}", id);
        return courseTeacherService.getCourseTeacher(id);
    }
}
