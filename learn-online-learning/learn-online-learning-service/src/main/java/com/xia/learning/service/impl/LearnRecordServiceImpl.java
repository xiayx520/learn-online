package com.xia.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xia.base.model.PageResult;
import com.xia.content.model.po.CoursePublish;
import com.xia.learning.feignclient.ContentServiceClient;
import com.xia.learning.feignclient.UserServiceClient;
import com.xia.learning.mapper.XcLearnRecordMapper;
import com.xia.learning.model.dto.LearnRecordDTO;
import com.xia.learning.model.dto.LearnRecordQueryDTO;
import com.xia.learning.model.po.XcLearnRecord;
import com.xia.learning.model.vo.LearnRankItemVO;
import com.xia.learning.model.vo.LearnRecordVO;
import com.xia.learning.model.vo.LearnStatisticsVO;
import com.xia.learning.service.LearnRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学习记录服务实现类
 */
@Slf4j
@Service
public class LearnRecordServiceImpl implements LearnRecordService {

    @Autowired
    private XcLearnRecordMapper learnRecordMapper;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;


    @Override
    @Transactional
    public Long saveLearnRecord(String userId, LearnRecordDTO learnRecordDTO) {
        if (userId == null || learnRecordDTO == null) {
            throw new IllegalArgumentException("用户ID和学习记录不能为空");
        }

        // 获取课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(learnRecordDTO.getCourseId());
        if (coursePublish == null) {
            log.error("课程信息不存在，课程ID：{}", learnRecordDTO.getCourseId());
            throw new RuntimeException("课程信息不存在");
        }

        // 查询是否存在记录
        LambdaQueryWrapper<XcLearnRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcLearnRecord::getUserId, userId)
                .eq(XcLearnRecord::getCourseId, learnRecordDTO.getCourseId())
                .eq(XcLearnRecord::getTeachplanId, learnRecordDTO.getTeachplanId());
        
        XcLearnRecord learnRecord = learnRecordMapper.selectOne(queryWrapper);

        if (learnRecord == null) {
            // 创建新记录
            learnRecord = new XcLearnRecord();
            learnRecord.setUserId(userId);
            learnRecord.setCourseId(learnRecordDTO.getCourseId());
            learnRecord.setCourseName(coursePublish.getName());
            learnRecord.setTeachplanId(learnRecordDTO.getTeachplanId());
            
            // 使用DTO中的章节名称，如果为空则使用默认值
            if (learnRecordDTO.getTeachplanName() != null && !learnRecordDTO.getTeachplanName().isEmpty()) {
                learnRecord.setTeachplanName(learnRecordDTO.getTeachplanName());
            } else {
                // 从课程发布信息中获取章节名称
                // 这里简化处理，实际应该解析coursePublish.getTeachplan()中的JSON数据获取具体章节名称
                learnRecord.setTeachplanName("章节" + learnRecordDTO.getTeachplanId());
            }
            
            learnRecord.setLearnLength(learnRecordDTO.getLearnLength());
            learnRecord.setLearnDate(LocalDateTime.now());
            
            learnRecordMapper.insert(learnRecord);
        } else {
            // 更新记录
            learnRecord.setLearnLength(learnRecord.getLearnLength() + learnRecordDTO.getLearnLength());
            learnRecord.setLearnDate(LocalDateTime.now());
            
            learnRecordMapper.updateById(learnRecord);
        }
        
        return learnRecord.getId();
    }

    @Override
    public PageResult<LearnRecordVO> queryLearnRecords(LearnRecordQueryDTO queryDTO) {
        if (queryDTO == null) {
            return new PageResult<LearnRecordVO>(new ArrayList<>(), 0L, 0L, 0L);
        }

        // 构建查询条件
        LambdaQueryWrapper<XcLearnRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (queryDTO.getCourseId() != null) {
            queryWrapper.eq(XcLearnRecord::getCourseId, queryDTO.getCourseId());
        }
        
        if (StringUtils.hasText(queryDTO.getUserId())) {
            queryWrapper.eq(XcLearnRecord::getUserId, queryDTO.getUserId());
        }
        
        // 处理日期查询条件
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            try {
                LocalDateTime startDate = LocalDateTime.parse(queryDTO.getStartDate().replace(" ", "T"));
                queryWrapper.ge(XcLearnRecord::getLearnDate, startDate);
            } catch (Exception e) {
                log.error("日期格式转换错误: {}", queryDTO.getStartDate(), e);
                // 在实际应用中可能需要抛出异常或给出友好提示
            }
        }
        
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            try {
                LocalDateTime endDate = LocalDateTime.parse(queryDTO.getEndDate().replace(" ", "T"));
                queryWrapper.le(XcLearnRecord::getLearnDate, endDate);
            } catch (Exception e) {
                log.error("日期格式转换错误: {}", queryDTO.getEndDate(), e);
                // 在实际应用中可能需要抛出异常或给出友好提示
            }
        }
        
        // 课程名称模糊查询
        if (StringUtils.hasText(queryDTO.getCourseName())) {
            queryWrapper.like(XcLearnRecord::getCourseName, queryDTO.getCourseName());
        }
        
        // 按照最近学习时间倒序排序
        queryWrapper.orderByDesc(XcLearnRecord::getLearnDate);
        
        // 分页查询
        Page<XcLearnRecord> page = new Page<>(queryDTO.getPageNo(), queryDTO.getPageSize());
        Page<XcLearnRecord> pageResult = learnRecordMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<LearnRecordVO> records = new ArrayList<>();
        for (XcLearnRecord record : pageResult.getRecords()) {
            LearnRecordVO vo = new LearnRecordVO();
            BeanUtils.copyProperties(record, vo);
            
            // 设置用户ID
            vo.setUserId(record.getUserId());
            
            // 设置日期为字符串格式
            if (record.getLearnDate() != null) {
                vo.setLearnDate(record.getLearnDate().toString().replace("T", " "));
            }
            
            // 计算学习进度（实际项目中可能需要根据章节总数来计算）
            // 这里简化处理，假设每个课程有10个章节
            vo.setLearnProgress(10.0); // 示例数据，实际应该计算实际进度
            
            records.add(vo);
        }
        
        return new PageResult<LearnRecordVO>(
            records, 
            pageResult.getTotal(), 
            (long) queryDTO.getPageNo(), 
            (long) queryDTO.getPageSize()
        );
    }

    @Override
    public LearnStatisticsVO getLearnStatistics(String userId, Long courseId) {
        return learnRecordMapper.selectLearnStatistics(userId, courseId);
    }

    @Override
    public Long getTotalLearnLength(String userId) {
        return learnRecordMapper.selectTotalLearnLength(userId);
    }

    @Override
    public List<LearnRankItemVO> getLearnLengthRanking(Long courseId) {
        // 获取学习时长排行榜
        List<LearnRankItemVO> result = learnRecordMapper.selectLearnLengthRanking(courseId);
        
        // 如果没有数据，返回空列表
        if (result == null || result.isEmpty()) {
            log.warn("未查询到学习时长排行榜数据");
            return new ArrayList<>();
        }
        //取出用户ids
        List<String> userIds = result.stream().map(LearnRankItemVO::getUserId).collect(Collectors.toList());

        //远程调用
        Map<String, String> userNameMap = userServiceClient.getUserName(userIds);

        for (LearnRankItemVO item : result) {
            String userName = userNameMap.get(item.getUserId());
            if (userName != null) {
                item.setUserName(userName);
            }
        }
        return result;
    }
    
    @Override
    public List<LearnRankItemVO> getCompletionRateRanking(Long courseId) {
        // 获取完成率排行榜
        List<LearnRankItemVO> result = learnRecordMapper.selectCompletionRateRanking(courseId);
        
        // 如果没有数据，返回空列表
        if (result == null || result.isEmpty()) {
            log.warn("未查询到完成率排行榜数据");
            return new ArrayList<>();
        }
        
        return result;
    }
} 