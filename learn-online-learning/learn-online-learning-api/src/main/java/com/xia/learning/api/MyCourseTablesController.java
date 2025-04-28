package com.xia.learning.api;

import com.xia.base.model.PageResult;
import com.xia.learning.model.dto.MyCourseTableParams;
import com.xia.learning.model.po.XcCourseTables;
import com.xia.learning.model.vo.XcChooseCourseVO;
import com.xia.learning.model.vo.XcCourseTablesVO;
import com.xia.learning.service.XcChooseCourseService;
import com.xia.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @description 我的课程表接口
 * @date 2022/10/25 9:40
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    private XcChooseCourseService xcChooseCourseService;


    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseVO addChooseCourse(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if (user == null) {
            throw new RuntimeException("请登录后继续选课");
        }
        userId = user.getId();
        return xcChooseCourseService.addChooseCourse(courseId, userId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesVO getLearnstatus(@PathVariable("courseId") Long courseId) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if (user == null) {
            throw new RuntimeException("请登录后继续选课");
        }
        userId = user.getId();
        return xcChooseCourseService.getLearningStatus(userId, courseId);

    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        return null;
    }

}
