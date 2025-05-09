package com.xia.learning.service;

import com.xia.base.model.PageResult;
import com.xia.learning.model.dto.LearnRecordDTO;
import com.xia.learning.model.dto.LearnRecordQueryDTO;
import com.xia.learning.model.vo.LearnRecordVO;
import com.xia.learning.model.vo.LearnRankItemVO;
import com.xia.learning.model.vo.LearnStatisticsVO;

import java.util.List;

/**
 * 学习记录服务接口
 */
public interface LearnRecordService {

    /**
     * 记录学习进度
     * @param userId 用户ID
     * @param learnRecordDTO 学习记录DTO
     * @return 记录ID
     */
    Long saveLearnRecord(String userId, LearnRecordDTO learnRecordDTO);

    /**
     * 查询学习记录列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<LearnRecordVO> queryLearnRecords(LearnRecordQueryDTO queryDTO);

    /**
     * 获取课程学习统计信息
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 学习统计信息
     */
    LearnStatisticsVO getLearnStatistics(String userId, Long courseId);

    /**
     * 获取用户总学习时长
     * @param userId 用户ID
     * @return 总学习时长(秒)
     */
    Long getTotalLearnLength(String userId);

    /**
     * 获取学习时长排行榜
     * @param courseId 课程ID，可为null表示全部课程
     * @return 排行榜列表
     */
    List<LearnRankItemVO> getLearnLengthRanking(Long courseId);

    /**
     * 获取完成率排行榜
     * @param courseId 课程ID，可为null表示全部课程
     * @return 排行榜列表
     */
    List<LearnRankItemVO> getCompletionRateRanking(Long courseId);
} 