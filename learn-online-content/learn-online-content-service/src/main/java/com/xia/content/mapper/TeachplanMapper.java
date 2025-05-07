package com.xia.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xia.content.model.po.Teachplan;
import com.xia.content.model.vo.TeachPlanVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * 根据课程id查询课程计划
     * @param courseId 课程id
     * @return 课程计划
     */
    List<TeachPlanVO> getTeachPlanTree(Long courseId);
}
