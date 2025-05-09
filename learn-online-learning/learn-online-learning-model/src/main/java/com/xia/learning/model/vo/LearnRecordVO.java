package com.xia.learning.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习记录视图对象
 */
@Data
@ApiModel(value="LearnRecordVO", description="学习记录视图对象")
public class LearnRecordVO {

    @ApiModelProperty(value = "记录ID")
    private Long id;

    @ApiModelProperty(value = "课程id")
    private Long courseId;

    @ApiModelProperty(value = "课程名称")
    private String courseName;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "章节id")
    private Long teachplanId;

    @ApiModelProperty(value = "章节名称")
    private String teachplanName;

    @ApiModelProperty(value = "最近学习时间")
    private String learnDate;

    @ApiModelProperty(value = "学习时长(秒)")
    private Long learnLength;

    @ApiModelProperty(value = "学习进度(百分比)")
    private Double learnProgress;
}