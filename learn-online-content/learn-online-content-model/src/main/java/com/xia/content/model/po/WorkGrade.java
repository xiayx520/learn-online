package com.xia.content.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业评分实体类
 */
@Data
@TableName("work_grade")
public class WorkGrade {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作业记录ID
     */
    private Long workRecordId;

    /**
     * 分数
     */
    private BigDecimal grade;

    /**
     * 评语
     */
    private String feedback;

    /**
     * 评分人ID
     */
    private Long graderId;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 修改时间
     */
    private LocalDateTime changeDate;
}