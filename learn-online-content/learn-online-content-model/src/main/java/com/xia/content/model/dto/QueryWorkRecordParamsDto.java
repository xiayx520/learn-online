package com.xia.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 作业提交记录查询参数DTO
 */
@Data
@ApiModel(value="作业提交记录查询参数", description="查询作业提交记录时的参数")
public class QueryWorkRecordParamsDto {

    @ApiModelProperty(value = "作业ID", required = true)
    private Long workId;
}