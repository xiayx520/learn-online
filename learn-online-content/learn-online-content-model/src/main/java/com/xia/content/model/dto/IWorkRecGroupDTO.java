package com.xia.content.model.dto;

import com.xia.content.model.vo.WorkRecordVO;
import lombok.Data;
import java.util.List;

/**
 * 作业批改分组 DTO
 */
@Data
public class IWorkRecGroupDTO {
    /**
     * 课程计划ID
     */
    private Long teachplanId;

    /**
     * 课程计划名称
     */
    private String teachplanName;

    /**
     * 作业提交记录列表
     */
    private List<WorkRecordVO> workRecordList;
} 