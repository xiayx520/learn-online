package com.xia.content.api;

import com.xia.content.model.vo.CourseCategoryTreeVO;
import com.xia.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "课程分类管理")
public class CourseCategoryController {
    @Autowired
    private CourseCategoryService courseCategoryService;

    /**
     * 查询课程分类
     * @return
     */
    @ApiOperation("查询课程分类")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeVO> queryTreeNodes() {
        log.info("查询课程分类");
        return courseCategoryService.queryTreeNodes("1");
    }
}
