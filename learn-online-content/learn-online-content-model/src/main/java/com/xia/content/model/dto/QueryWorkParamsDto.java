package com.xia.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="作业查询参数", description="作业查询参数")
public class QueryWorkParamsDto {

    @ApiModelProperty("作业状态")
    private String status;
}