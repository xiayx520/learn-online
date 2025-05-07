package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xia.base.exception.GlobalException;
import com.xia.content.mapper.TeachplanMapper;
import com.xia.content.mapper.TeachplanWorkMapper;
import com.xia.content.mapper.WorkMapper;
import com.xia.content.model.po.Teachplan;
import com.xia.content.model.po.TeachplanWork;
import com.xia.content.model.po.WorkInfo;
import com.xia.content.service.TeachplanWorkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TeachplanWorkServiceImpl implements TeachplanWorkService {

    @Autowired
    private TeachplanWorkMapper teachplanWorkMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private WorkMapper workMapper;

    @Override
    @Transactional
    public void bindWorkToTeachplan(Long workId, List<Long> teachplanIds) {
        // 1. 校验作业是否存在
        WorkInfo workInfo = workMapper.selectById(workId);
        if (workInfo == null) {
            throw new GlobalException("作业不存在");
        }

        // 2. 校验课程计划是否存在，并且必须是小章节
        for (Long teachplanId : teachplanIds) {
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            if (teachplan == null) {
                throw new GlobalException("课程计划不存在");
            }
            
            // 检查是否是小章节（grade == 2）
            if (teachplan.getGrade() != 2) {
                throw new GlobalException("只能将作业绑定到小章节");
            }

            // 3. 检查是否已经绑定其他作业
            LambdaQueryWrapper<TeachplanWork> existingBindings = new LambdaQueryWrapper<>();
            existingBindings.eq(TeachplanWork::getTeachplanId, teachplanId);
            List<TeachplanWork> bindings = teachplanWorkMapper.selectList(existingBindings);
            
            if (!bindings.isEmpty()) {
                // 检查是否已绑定相同的作业
                boolean alreadyBoundToSameWork = bindings.stream()
                    .anyMatch(binding -> binding.getWorkId().equals(workId));
                
                if (alreadyBoundToSameWork) {
                    log.warn("课程计划[{}]已绑定作业[{}]，跳过重复绑定", teachplanId, workId);
                    continue; // 跳过重复绑定
                }
                
                throw new GlobalException("该章节已绑定其他作业");
            }

            // 4. 创建绑定关系
            TeachplanWork teachplanWork = new TeachplanWork();
            teachplanWork.setWorkId(workId);
            teachplanWork.setWorkTitle(workInfo.getTitle());
            teachplanWork.setTeachplanId(teachplanId);
            teachplanWork.setCourseId(teachplan.getCourseId());
            teachplanWork.setCoursePubId(teachplan.getCoursePubId());
            teachplanWork.setCreateDate(LocalDateTime.now());

            teachplanWorkMapper.insert(teachplanWork);
        }
    }

    @Override
    public List<TeachplanWork> getTeachplansByWorkId(Long workId) {
        LambdaQueryWrapper<TeachplanWork> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanWork::getWorkId, workId);
        return teachplanWorkMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void unbindWorkFromTeachplan(Long workId, Long teachplanId) {
        LambdaQueryWrapper<TeachplanWork> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanWork::getWorkId, workId)
                .eq(TeachplanWork::getTeachplanId, teachplanId);
        teachplanWorkMapper.delete(queryWrapper);
    }
} 