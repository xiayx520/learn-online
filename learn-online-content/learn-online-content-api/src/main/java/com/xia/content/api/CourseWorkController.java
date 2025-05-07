package com.xia.content.api;

import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程作业控制器
 */
@RestController
@Slf4j
@RequestMapping("/course-work")
@Api(tags = "课程作业管理")
public class CourseWorkController {

    @Autowired
    private TeachPlanService teachPlanService;

    /**
     * 获取课程计划及关联的作业信息
     * @param courseId 课程ID
     * @return 课程计划及作业信息
     */
    @GetMapping("/{courseId}")
    @ApiOperation("获取课程计划及关联的作业信息")
    public List<TeachPlanVO> getCourseWorkInfo(@PathVariable Long courseId) {
        log.info("获取课程[{}]的作业信息", courseId);
        return teachPlanService.getTeachPlanTreeWithWork(courseId);
    }
} 