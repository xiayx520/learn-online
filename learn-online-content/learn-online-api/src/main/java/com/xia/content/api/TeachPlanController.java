package com.xia.content.api;


import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.TeachPlanService;
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
@Api(tags = "课程计划管理")
public class TeachPlanController {

    @Autowired
    private TeachPlanService teachPlanService;


    /**
     * 获取课程计划树形结构
     * @param courseId 课程id
     * @return 课程计划树形结构
     */
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation("获取课程计划树形结构")
    public List<TeachPlanVO> getTeachPlanTree(@PathVariable Long courseId) {
        return teachPlanService.getTeachPlanTree(courseId);
    }
}

