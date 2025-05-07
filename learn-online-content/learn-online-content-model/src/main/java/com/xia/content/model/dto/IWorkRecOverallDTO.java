package com.xia.content.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 作业批改详情 DTO
 */
@Data
public class IWorkRecOverallDTO {
    /**
     * 作业ID
     */
    private Long workId;

    /**
     * 作业标题
     */
    private String workTitle;

    /**
     * 提交总数
     */
    private Integer totalSubmissions;

    /**
     * 待批改数
     */
    private Integer pendingReviews;

    /**
     * 按课程计划分组的提交记录
     */
    private List<IWorkRecGroupDTO> recGroupDTOList;
} 