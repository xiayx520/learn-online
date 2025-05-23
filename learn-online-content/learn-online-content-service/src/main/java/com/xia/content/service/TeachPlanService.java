package com.xia.content.service;

import com.xia.content.model.dto.BindTeachplanMediaDto;
import com.xia.content.model.dto.SaveTeachplanDto;
import com.xia.content.model.vo.TeachPlanVO;

import java.util.List;

public interface TeachPlanService {

    /**
     * 根据课程id查询课程计划
     * @param courseId 课程id
     * @return 课程计划
     */
    List<TeachPlanVO> getTeachPlanTree(Long courseId);

    /**
     * 获取课程计划树形结构，包含作业信息
     * @param courseId 课程id
     * @return 课程计划树形结构(包含作业信息)
     */
    List<TeachPlanVO> getTeachPlanTreeWithWork(Long courseId);

    /**
     * 保存课程计划
     * @param saveTeachplanDto
     */
    void saveTeachPlan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     * @param id
     */
    void deleteTeachPlan(Long id);


    /**
     * 移动课程计划
     * @param moveType
     * @param id
     */
    void moveTeachPlan(String moveType, Long id);


    /**
     * 绑定媒资
     * @param bindTeachplanMediaDto
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);


    /**
     * 解绑媒资
     * @param teachPlanId
     * @param mediaId
     */
    void deleteTeachPlanMedia(Long teachPlanId, String mediaId);

    /**
     * 判断是否可以预览
     * @param teachPlanId
     * @return
     */
    Boolean isPreview(Long teachPlanId);
}
