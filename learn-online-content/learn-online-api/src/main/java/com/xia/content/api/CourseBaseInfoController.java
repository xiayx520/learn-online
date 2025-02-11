package com.xia.content.api;

import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.model.dto.QueryCourseParamsDto;
import com.xia.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseBaseInfoController {
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto)
    {
        return null;
    }

}
