package com.xia.learning.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学习统计视图对象
 */
@Data
@ApiModel(value="LearnStatisticsVO", description="学习统计视图对象")
public class LearnStatisticsVO {

    @ApiModelProperty(value = "课程id")
    private Long courseId;

    @ApiModelProperty(value = "课程名称")
    private String courseName;

    @ApiModelProperty(value = "总学习时长(秒)")
    private Long totalLearnLength;

    @ApiModelProperty(value = "总章节数")
    private Integer totalChapters;

    @ApiModelProperty(value = "已完成章节数")
    private Integer completedChapters;

    @ApiModelProperty(value = "完成率(百分比)")
    private Double completionRate;
} 