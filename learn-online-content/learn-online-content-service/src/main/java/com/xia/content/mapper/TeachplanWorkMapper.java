package com.xia.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xia.content.model.po.TeachplanWork;
import com.xia.content.model.vo.TeachplanWorkVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程计划作业关联 Mapper 接口
 */
@Mapper
public interface TeachplanWorkMapper extends BaseMapper<TeachplanWork> {
    
    /**
     * 根据课程ID查询课程计划关联的作业信息
     * @param courseId 课程ID
     * @return 课程计划作业关联信息列表
     */
    List<TeachplanWorkVO> getTeachplanWorksByCourseId(@Param("courseId") Long courseId);
}