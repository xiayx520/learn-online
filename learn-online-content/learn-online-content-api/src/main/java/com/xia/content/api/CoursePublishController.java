package com.xia.content.api;

import com.alibaba.fastjson.JSON;
import com.xia.content.model.po.CoursePublish;
import com.xia.content.model.po.CourseTeacher;
import com.xia.content.model.vo.CourseBaseInfoVO;
import com.xia.content.model.vo.CoursePreviewVO;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@Slf4j
@Api(tags = "课程发布管理")
public class CoursePublishController {

    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        //设置模型数据
        modelAndView.addObject("name","小明");
        //设置模板名称
        modelAndView.setViewName("test");
        return modelAndView;
    }


    /**
     * 课程发布预览
     * @param courseId
     * @return
     */
    @ApiOperation("课程发布预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable Long courseId){
        //获取课程预览信息
        CoursePreviewVO coursePublishVO = coursePublishService.getCoursePreviewVO(courseId);
        //设置模型数据
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model", coursePublishVO);
        //设置模板名称
        modelAndView.setViewName("course_template");
        return modelAndView;
    }


    /**
     * 课程提交审核
     * @param courseId
     */
    @PostMapping("/courseaudit/commit/{courseId}")
    @ApiOperation("课程提交审核")
    @ResponseBody
    public void commitAudit(@PathVariable("courseId") Long courseId){
        log.info("课程提交审核：{}", courseId);
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }


    /**
     * 课程发布
     * @param courseId
     */
    @PostMapping("/coursepublish/{courseId}")
    @ApiOperation("课程发布")
    @ResponseBody
    public void publishCourse(@PathVariable("courseId") Long courseId){
        log.info("课程发布：{}", courseId);
        Long companyId = 1232141425L;
        coursePublishService.publishCourse(companyId, courseId);
    }


    /**
     * 查询课程发布信息
     * @param courseId
     * @return
     */
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }


    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewVO getCoursePublish(@PathVariable("courseId") Long courseId) {
        //查询课程发布信息
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        if (coursePublish == null) {
            return new CoursePreviewVO();
        }

        //课程基本信息
        CourseBaseInfoVO courseBase = new CourseBaseInfoVO();
        BeanUtils.copyProperties(coursePublish, courseBase);
        //课程计划
        List<TeachPlanVO> teachplans = JSON.parseArray(coursePublish.getTeachplan(), TeachPlanVO.class);
        //师资信息
        List<CourseTeacher> courseTeachers = JSON.parseArray(coursePublish.getTeachers(), CourseTeacher.class);
        CoursePreviewVO coursePublishInfo = new CoursePreviewVO();
        coursePublishInfo.setCourseBase(courseBase);
        coursePublishInfo.setTeachplans(teachplans);
        coursePublishInfo.setCourseTeacher(courseTeachers);
        return coursePublishInfo;
    }
}
