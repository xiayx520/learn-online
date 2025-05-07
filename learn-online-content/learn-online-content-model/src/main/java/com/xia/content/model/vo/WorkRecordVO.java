package com.xia.content.model.vo;

import com.xia.content.model.po.WorkRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 作业记录展示 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkRecordVO extends WorkRecord {

    /**
     * 用户名
     */
    private String username;

    /**
     * 课程计划名称
     */
    private String teachplanName;

    /**
     * 作业题目
     */
    private String question;

    /**
     * 作业答案
     */
    private String answer;

    /**
     * 评语
     */
    private String correctComment;

    /**
     * 课程发布ID
     */
    private Long coursePubId;

    /**
     * 分数
     */
    private BigDecimal grade;
}