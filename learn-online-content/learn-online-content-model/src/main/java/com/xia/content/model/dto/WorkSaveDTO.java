package com.xia.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 作业保存请求DTO
 */
@Data
@ApiModel(value="作业保存请求", description="创建和更新作业时的数据传输对象")
public class WorkSaveDTO {

    @ApiModelProperty(value = "作业ID（更新时需要）")
    private Long id;

    @NotEmpty(message = "作业标题不能为空")
    @ApiModelProperty(value = "作业标题", required = true)
    private String title;

    @ApiModelProperty(value = "作业描述")
    private String description;
}