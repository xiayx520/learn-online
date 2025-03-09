package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xia.content.mapper.TeachplanMapper;
import com.xia.content.model.dto.SaveTeachplanDto;
import com.xia.content.model.po.Teachplan;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    /**
     * 获取课程计划
     *
     * @param courseId 课程id
     * @return 课程计划
     */
    @Override
    public List<TeachPlanVO> getTeachPlanTree(Long courseId) {
        return teachplanMapper.getTeachPlanTree(courseId);
    }

    /**
     * 保存课程计划
     *
     * @param saveTeachplanDto
     */
    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        //课程计划id
        Long id = saveTeachplanDto.getId();
        //修改课程计划
        if (id != null) {
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        } else {
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count + 1);
            BeanUtils.copyProperties(saveTeachplanDto, teachplanNew);

            teachplanMapper.insert(teachplanNew);
        }
    }

    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }


}
