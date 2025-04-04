package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xia.base.exception.GlobalException;
import com.xia.content.mapper.TeachplanMapper;
import com.xia.content.mapper.TeachplanMediaMapper;
import com.xia.content.model.dto.SaveTeachplanDto;
import com.xia.content.model.po.Teachplan;
import com.xia.content.model.po.TeachplanMedia;
import com.xia.content.model.vo.TeachPlanVO;
import com.xia.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

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

    /**
     * 删除课程计划
     * @param id
     */
    @Override
    @Transactional
    public void deleteTeachPlan(Long id) {
        //判断id对应的课程计划是否存在
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            throw new GlobalException("课程计划不存在");
        }
        //判断课程计划是否为父级课程计划,如果是父级课程计划则要判断改父级课程下是否有子级计划，如果有则不允许删除
        if (teachplan.getParentid() == 0) {
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, id);
            queryWrapper.eq(Teachplan::getStatus, 1);
            int count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new GlobalException("该课程计划下有子级课程计划，不允许删除");
            }
        }
        //删除小章节，同时将关联的信息进行删除。
        teachplan.setStatus(0);
        teachplanMapper.updateById(teachplan);
        //删除课程计划媒资信息
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplan.getId()));
    }

    /**
     * 移动课程计划
     * @param moveType
     * @param id
     */
    @Override
    @Transactional
    public void moveTeachPlan(String moveType, Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            throw new GlobalException("课程计划不存在");
        }
        //查询该课程下同父节点的全部的课程计划，升序排序
        List<Teachplan> teachplans = teachplanMapper.selectList(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getParentid, teachplan.getParentid())
                .eq(Teachplan::getStatus, 1)
                .orderByAsc(Teachplan::getOrderby));
        //判断移动类型
        if (moveType.equals("moveup")) {
            for (int i = 0; i < teachplans.size(); i++) {
                Teachplan teachplan1 = teachplans.get(i);
                if (teachplan1.getId().equals(id)) {
                    if (i == 0) {
                        throw new GlobalException("该课程计划已经是同级课程计划中的第一个");
                    }
                    Teachplan teachplan2 = teachplans.get(i - 1);
                    int orderby = teachplan1.getOrderby();
                    teachplan1.setOrderby(teachplan2.getOrderby());
                    teachplan2.setOrderby(orderby);
                    teachplanMapper.updateById(teachplan1);
                    teachplanMapper.updateById(teachplan2);
                }
            }
        } else {
            for (int i = 0; i < teachplans.size(); i++) {
                Teachplan teachplan1 = teachplans.get(i);
                if (teachplan1.getId().equals(id)) {
                    if (i == teachplans.size() - 1) {
                        throw new GlobalException("该课程计划已经是同级课程计划中的最后一个");
                    }
                    Teachplan teachplan2 = teachplans.get(i + 1);
                    int orderby = teachplan1.getOrderby();
                    teachplan1.setOrderby(teachplan2.getOrderby());
                    teachplan2.setOrderby(orderby);
                    teachplanMapper.updateById(teachplan1);
                    teachplanMapper.updateById(teachplan2);
                }
            }
        }
    }

    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }


}
