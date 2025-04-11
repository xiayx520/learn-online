package com.xia.content.api;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@Api(tags = "课程发布管理")
public class CoursePublishController {

    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        //设置模型数据
        modelAndView.addObject("name","小明");
        //设置模板名称
        modelAndView.setViewName("test");
        return modelAndView;
    }
}
