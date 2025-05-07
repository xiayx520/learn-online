package com.xia.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 作业评分请求DTO
 */
@Data
@ApiModel(value="作业评分请求", description="教师评分作业时的数据传输对象")
public class WorkGradeDTO {

    @NotNull(message = "作业记录ID不能为空")
    @ApiModelProperty(value = "作业记录ID", required = true)
    private Long workRecordId;

    @NotNull(message = "分数不能为空")
    @DecimalMin(value = "0.0", message = "分数不能小于0分")
    @DecimalMax(value = "100.0", message = "分数不能大于100分")
    @ApiModelProperty(value = "分数", required = true)
    private BigDecimal grade;

    @ApiModelProperty(value = "评语")
    private String feedback;
}