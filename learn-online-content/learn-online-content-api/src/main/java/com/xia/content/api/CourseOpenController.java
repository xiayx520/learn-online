package com.xia.content.api;

import com.xia.content.model.vo.CoursePreviewVO;
import com.xia.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "课程公开查询接口")
@RequestMapping("/open")
@Slf4j
@RestController
public class CourseOpenController {


    @Autowired
    private CoursePublishService coursePublishService;


    /**
     * 获取课程发布信息
     * @param courseId
     * @return
     */
    @ApiOperation("获取课程发布信息")
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewVO getCourseWholeInfo(@PathVariable Long courseId) {
        log.debug("获取课程发布信息,courseId:{}", courseId);
        if (courseId == null) {
            throw new RuntimeException("课程id为空");
        }
        return coursePublishService.getCoursePreviewVO(courseId);
    }
}
