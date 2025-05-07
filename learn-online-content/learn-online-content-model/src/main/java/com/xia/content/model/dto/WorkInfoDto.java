package com.xia.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="作业信息DTO", description="作业信息") 
public class WorkInfoDto {

    @ApiModelProperty(value = "作业ID", required = false)
    private Long id;

    @ApiModelProperty(value = "作业标题", required = true)
    private String title;

    @ApiModelProperty(value = "作业描述", required = false)
    private String description;
}