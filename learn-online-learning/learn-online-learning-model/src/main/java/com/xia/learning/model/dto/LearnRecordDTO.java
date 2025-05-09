package com.xia.learning.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习记录请求参数DTO
 */
@Data
@ApiModel(value="LearnRecordDTO", description="学习记录参数")
public class LearnRecordDTO {

    @ApiModelProperty(value = "课程id", required = true)
    private Long courseId;

    @ApiModelProperty(value = "章节id", required = true)
    private Long teachplanId;

    @ApiModelProperty(value = "章节名称")
    private String teachplanName;

    @ApiModelProperty(value = "学习时长(秒)", required = true)
    private Long learnLength;

    @ApiModelProperty(value = "学习开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "学习结束时间")
    private LocalDateTime endTime;
}