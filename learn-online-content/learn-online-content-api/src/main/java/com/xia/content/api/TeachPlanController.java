package com.xia.content.api;


import com.xia.content.model.dto.BindTeachplanMediaDto;
import com.xia.content.model.dto.SaveTeachplanDto;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 保存课程计划
     * @param saveTeachplanDto
     */
    @PostMapping("/teachplan")
    @ApiOperation("保存课程计划")
    public void saveTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto) {
        teachPlanService.saveTeachPlan(saveTeachplanDto);
    }


    /**
     * 删除课程计划
     * @param id 课程计划id
     */
    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachPlan(@PathVariable Long id) {
        teachPlanService.deleteTeachPlan(id);
    }

    /**
     * 移动课程计划
     * @param moveType
     * @param id
     */
    @ApiOperation("移动课程计划")
    @PostMapping("/teachplan/{moveType}/{id}")
    public void moveTeachPlan(@PathVariable String moveType, @PathVariable Long id) {
        teachPlanService.moveTeachPlan(moveType, id);
    }


    /**
     * 绑定媒资信息
     * @param bindTeachplanMediaDto
     */
    @ApiOperation("绑定媒资信息")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachPlanService.associationMedia(bindTeachplanMediaDto);
    }


    /**
     * 删除课程计划媒资信息
     * @param teachPlanId
     * @param mediaId
     */
    @ApiOperation("删除课程计划媒资信息")
    @DeleteMapping("/teachplan//association/media/{teachPlanId}/{mediaId}")
    public void deleteTeachPlanMedia(@PathVariable Long teachPlanId, @PathVariable String mediaId) {
        teachPlanService.deleteTeachPlanMedia(teachPlanId, mediaId);
    }

    /**
     * 判断该课程计划是否支持试看
     * @param teachPlanId
     * @return
     */
    @ApiOperation("判断该课程计划是否支持试看")
    @GetMapping("/teachplan/isPreview/{teachPlanId}")
    public Boolean isPreview(@PathVariable("teachPlanId") Long teachPlanId) {
        return teachPlanService.isPreview(teachPlanId);
    }

}

