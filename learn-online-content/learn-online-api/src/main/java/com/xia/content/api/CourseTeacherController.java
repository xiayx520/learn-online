package com.xia.content.api;

import com.xia.content.model.po.CourseTeacher;
import com.xia.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 添加或修改课程教师信息
     * @param courseTeacher
     * @return
     */
    @PostMapping("/courseTeacher")
    @ApiOperation("添加或修改课程教师信息")
    public CourseTeacher addOrUpdateCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        log.info("添加或修改课程教师信息，参数：{}", courseTeacher);
        //TODO 机构id
        Long companyId = 1232141425L;
        return courseTeacherService.addOrUpdateCourseTeacher(courseTeacher, companyId);
    }

    /**
     * 根据课程id和教师id删除课程教师信息
     * @param courseId
     * @param teacherId
     */
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    @ApiOperation("根据课程id和教师id删除课程教师信息")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        log.info("根据课程id和教师id删除课程教师信息，参数courseId：{}, 参数teacherId：{}", courseId, teacherId);
        //TODO 机构id
        Long companyId = 1232141425L;
        courseTeacherService.deleteCourseTeacher(courseId, teacherId, companyId);
    }
}
