package com.xia.learning.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学习记录查询条件DTO
 */
@Data
@ApiModel(value="LearnRecordQueryDTO", description="学习记录查询条件")
public class LearnRecordQueryDTO {

    @ApiModelProperty(value = "课程id")
    private Long courseId;

    @ApiModelProperty(value = "课程名称")
    private String courseName;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "开始日期")
    private String startDate;

    @ApiModelProperty(value = "结束日期")
    private String endDate;

    @ApiModelProperty(value = "当前页码")
    private int pageNo = 1;

    @ApiModelProperty(value = "每页记录数")
    private int pageSize = 10;
}