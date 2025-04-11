package com.xia.content.api;

import com.xia.content.model.vo.CoursePreviewVO;
import com.xia.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

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
}
