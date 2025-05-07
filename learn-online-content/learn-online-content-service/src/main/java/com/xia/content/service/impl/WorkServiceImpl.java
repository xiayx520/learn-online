package com.xia.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xia.base.exception.GlobalException;
import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.content.mapper.WorkGradeMapper;
import com.xia.content.mapper.WorkMapper;
import com.xia.content.mapper.WorkRecordMapper;
import com.xia.content.model.dto.QueryWorkRecordParamsDto;
import com.xia.content.model.dto.WorkGradeDTO;
import com.xia.content.model.dto.WorkInfoDto;
import com.xia.content.model.dto.WorkSubmitDTO;
import com.xia.content.model.po.WorkGrade;
import com.xia.content.model.po.WorkInfo;
import com.xia.content.model.po.WorkRecord;
import com.xia.content.service.WorkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkServiceImpl implements WorkService {

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private WorkRecordMapper workRecordMapper;

    @Autowired
    private WorkGradeMapper workGradeMapper;

    // 作业状态常量
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final String STATUS_ARCHIVED = "archived";

    @Override
    @Transactional
    public WorkInfo saveWork(WorkInfoDto workInfoDto) {
        WorkInfo workInfo = new WorkInfo();
        BeanUtils.copyProperties(workInfoDto, workInfo);
        
        if (workInfo.getId() == null) {
            // 新增作业
            workInfo.setStatus(STATUS_DRAFT);
            workInfo.setCreateDate(LocalDateTime.now());
            workInfo.setChangeDate(LocalDateTime.now());
            int insert = workMapper.insert(workInfo);
            if (insert <= 0) {
                throw new GlobalException("新增作业失败");
            }
        } else {
            // 更新作业
            WorkInfo existWork = workMapper.selectById(workInfo.getId());
            if (existWork == null) {
                throw new GlobalException("作业不存在");
            }
            // 只有草稿状态的作业可以编辑
            if (!STATUS_DRAFT.equals(existWork.getStatus())) {
                throw new GlobalException("只有草稿状态的作业可以编辑");
            }
            workInfo.setStatus(STATUS_DRAFT); // 确保状态不变
            workInfo.setChangeDate(LocalDateTime.now());
            int update = workMapper.updateById(workInfo);
            if (update <= 0) {
                throw new GlobalException("更新作业失败");
            }
        }
        return workInfo;
    }

    @Override
    @Transactional
    public void publishWork(Long workId) {
        WorkInfo workInfo = workMapper.selectById(workId);
        if (workInfo == null) {
            throw new GlobalException("作业不存在");
        }
        // 只有草稿状态的作业可以发布
        if (!STATUS_DRAFT.equals(workInfo.getStatus())) {
            throw new GlobalException("只有草稿状态的作业可以发布");
        }
        workInfo.setStatus(STATUS_PUBLISHED);
        workInfo.setChangeDate(LocalDateTime.now());
        int update = workMapper.updateById(workInfo);
        if (update <= 0) {
            throw new GlobalException("发布作业失败");
        }
    }

    @Override
    @Transactional
    public void archiveWork(Long workId) {
        WorkInfo workInfo = workMapper.selectById(workId);
        if (workInfo == null) {
            throw new GlobalException("作业不存在");
        }
        // 只有发布状态的作业可以归档
        if (!STATUS_PUBLISHED.equals(workInfo.getStatus())) {
            throw new GlobalException("只有发布状态的作业可以归档");
        }
        workInfo.setStatus(STATUS_ARCHIVED);
        workInfo.setChangeDate(LocalDateTime.now());
        int update = workMapper.updateById(workInfo);
        if (update <= 0) {
            throw new GlobalException("归档作业失败");
        }
    }

    @Override
    public PageResult<WorkInfo> queryWorkList(PageParams pageParams, String status) {
        // 构建查询条件
        LambdaQueryWrapper<WorkInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据状态查询
        if (StringUtils.isNotEmpty(status)) {
            queryWrapper.eq(WorkInfo::getStatus, status);
        }
        
        // 按创建时间降序
        queryWrapper.orderByDesc(WorkInfo::getCreateDate);

        // 分页查询
        Page<WorkInfo> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<WorkInfo> workInfoPage = workMapper.selectPage(page, queryWrapper);

        // 封装结果
        return new PageResult<>(workInfoPage.getRecords(), workInfoPage.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    @Transactional
    public WorkRecord submitWork(WorkSubmitDTO workSubmitDTO) {
        // 校验作业是否存在且已发布
        WorkInfo workInfo = workMapper.selectById(workSubmitDTO.getWorkId());
        if (workInfo == null) {
            throw new GlobalException("作业不存在");
        }
        if (!"published".equals(workInfo.getStatus())) {
            throw new GlobalException("作业未发布，不能提交");
        }

        // 获取当前用户ID（实际项目中应从SecurityContext获取）
        Long userId = 1L; // TODO: 从SecurityContext获取当前用户ID

        // 查询是否已提交过
        LambdaQueryWrapper<WorkRecord> queryWrapper = new LambdaQueryWrapper<WorkRecord>()
                .eq(WorkRecord::getWorkId, workSubmitDTO.getWorkId())
                .eq(WorkRecord::getUserId, userId);
        WorkRecord existRecord = workRecordMapper.selectOne(queryWrapper);

        WorkRecord workRecord;
        if (existRecord != null) {
            // 更新提交记录
            workRecord = existRecord;
            workRecord.setSubmitContent(workSubmitDTO.getContent());
            workRecord.setSubmitDate(LocalDateTime.now());
            workRecord.setStatus("submitted");
            workRecord.setChangeDate(LocalDateTime.now());
            workRecordMapper.updateById(workRecord);
        } else {
            // 创建新的提交记录
            workRecord = new WorkRecord();
            workRecord.setWorkId(workSubmitDTO.getWorkId());
            workRecord.setUserId(userId);
            workRecord.setSubmitContent(workSubmitDTO.getContent());
            workRecord.setStatus("submitted");
            workRecord.setSubmitDate(LocalDateTime.now());
            workRecord.setCreateDate(LocalDateTime.now());
            workRecord.setChangeDate(LocalDateTime.now());
            workRecordMapper.insert(workRecord);
        }

        return workRecord;
    }

    @Override
    public PageResult<WorkRecord> queryWorkRecordList(PageParams pageParams, QueryWorkRecordParamsDto queryWorkRecordParamsDto) {
        // 构建查询条件
        LambdaQueryWrapper<WorkRecord> queryWrapper = new LambdaQueryWrapper<WorkRecord>()
                .eq(WorkRecord::getWorkId, queryWorkRecordParamsDto.getWorkId())
                .orderByDesc(WorkRecord::getCreateDate);

        // 分页查询
        Page<WorkRecord> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<WorkRecord> workRecordPage = workRecordMapper.selectPage(page, queryWrapper);

        // 封装结果

        return new PageResult<>(workRecordPage.getRecords(), workRecordPage.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grade(WorkGradeDTO workGradeDTO) {
        // 1. 校验作业记录是否存在
        WorkRecord workRecord = workRecordMapper.selectById(workGradeDTO.getWorkRecordId());
        if (workRecord == null) {
            throw new GlobalException("作业记录不存在");
        }

        // 2. 校验作业状态是否为已提交
        if (!"submitted".equals(workRecord.getStatus())) {
            throw new GlobalException("只能对已提交的作业进行评分");
        }

        // 3. 创建评分记录
        WorkGrade workGrade = new WorkGrade();
        BeanUtils.copyProperties(workGradeDTO, workGrade);
        workGrade.setCreateDate(LocalDateTime.now());
        workGrade.setChangeDate(LocalDateTime.now());
        // TODO: 设置评分人ID，需要从当前登录用户中获取
        workGrade.setGraderId(1L);

        // 4. 保存评分记录
        workGradeMapper.insert(workGrade);

        // 5. 更新作业记录状态为已评分
        workRecord.setStatus("graded");
        workRecord.setChangeDate(LocalDateTime.now());
        workRecordMapper.updateById(workRecord);

        log.info("作业评分完成，作业记录ID：{}，分数：{}", workGradeDTO.getWorkRecordId(), workGradeDTO.getGrade());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWork(Long workId) {
        // 1. 校验作业是否存在
        WorkInfo workInfo = workMapper.selectById(workId);
        if (workInfo == null) {
            throw new GlobalException("作业不存在");
        }
        // 只有草稿状态的作业可以删除
        if (!STATUS_DRAFT.equals(workInfo.getStatus())) {
            throw new GlobalException("只有草稿状态的作业可以删除");
        }

        // 2. 删除作业记录
        LambdaQueryWrapper<WorkRecord> recordQueryWrapper = new LambdaQueryWrapper<WorkRecord>()
                .eq(WorkRecord::getWorkId, workId);
        workRecordMapper.delete(recordQueryWrapper);

        // 3. 删除作业评分记录
        List<WorkRecord> records = workRecordMapper.selectList(recordQueryWrapper);
        if (!records.isEmpty()) {
            List<Long> recordIds = records.stream().map(WorkRecord::getId).collect(Collectors.toList());
            LambdaQueryWrapper<WorkGrade> gradeQueryWrapper = new LambdaQueryWrapper<WorkGrade>()
                    .in(WorkGrade::getWorkRecordId, recordIds);
            workGradeMapper.delete(gradeQueryWrapper);
        }

        // 4. 删除作业
        int delete = workMapper.deleteById(workId);
        if (delete <= 0) {
            throw new GlobalException("删除作业失败");
        }
    }

    @Override
    @Transactional
    public void unpublishWork(Long workId) {
        WorkInfo workInfo = workMapper.selectById(workId);
        if (workInfo == null) {
            throw new GlobalException("作业不存在");
        }
        // 只有发布状态的作业可以下线
        if (!STATUS_PUBLISHED.equals(workInfo.getStatus())) {
            throw new GlobalException("只有发布状态的作业可以下线");
        }
        workInfo.setStatus(STATUS_DRAFT);
        workInfo.setChangeDate(LocalDateTime.now());
        int update = workMapper.updateById(workInfo);
        if (update <= 0) {
            throw new GlobalException("下线作业失败");
        }
    }

    @Override
    @Transactional
    public void unarchiveWork(Long workId) {
        WorkInfo workInfo = workMapper.selectById(workId);
        if (workInfo == null) {
            throw new GlobalException("作业不存在");
        }
        // 只有归档状态的作业可以取消归档
        if (!STATUS_ARCHIVED.equals(workInfo.getStatus())) {
            throw new GlobalException("只有归档状态的作业可以取消归档");
        }
        workInfo.setStatus(STATUS_PUBLISHED);
        workInfo.setChangeDate(LocalDateTime.now());
        int update = workMapper.updateById(workInfo);
        if (update <= 0) {
            throw new GlobalException("取消归档失败");
        }
    }
}