package com.xia.content.model.vo;

import com.xia.content.model.po.TeachplanWork;
import com.xia.content.model.po.WorkInfo;
import lombok.Data;

/**
 * 课程计划作业VO，包含作业详情
 */
@Data
public class TeachplanWorkVO extends TeachplanWork {
    // 作业详细信息
    private WorkInfo workInfo;
} 