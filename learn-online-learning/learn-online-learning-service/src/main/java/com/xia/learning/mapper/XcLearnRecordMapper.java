package com.xia.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xia.learning.model.po.XcLearnRecord;
import com.xia.learning.model.vo.LearnRankItemVO;
import com.xia.learning.model.vo.LearnStatisticsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习记录数据访问接口
 */
public interface XcLearnRecordMapper extends BaseMapper<XcLearnRecord> {

    /**
     * 统计用户课程学习情况
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 学习统计信息
     */
    LearnStatisticsVO selectLearnStatistics(@Param("userId") String userId, @Param("courseId") Long courseId);

    /**
     * 查询用户的总学习时长
     * @param userId 用户ID
     * @return 总学习时长(秒)
     */
    Long selectTotalLearnLength(@Param("userId") String userId);

    /**
     * 根据课程ID查询已完成的章节数
     * @param userId 用户ID
     * @param courseId 课程ID 
     * @return 已完成章节数
     */
    Integer selectCompletedChapters(@Param("userId") String userId, @Param("courseId") Long courseId);
    
    /**
     * 查询学习时长排行榜
     * @param courseId 课程ID，可为null表示全部课程
     * @return 排行榜列表
     */
    List<LearnRankItemVO> selectLearnLengthRanking(@Param("courseId") Long courseId);
    
    /**
     * 查询完成率排行榜
     * @param courseId 课程ID，可为null表示全部课程
     * @return 排行榜列表
     */
    List<LearnRankItemVO> selectCompletionRateRanking(@Param("courseId") Long courseId);
} 