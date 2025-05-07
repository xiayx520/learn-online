package com.xia.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 作业提交请求DTO
 */
@Data
@ApiModel(value="作业提交请求", description="学生提交作业时的数据传输对象")
public class WorkSubmitDTO {

    @NotNull(message = "作业ID不能为空")
    @ApiModelProperty(value = "作业ID", required = true)
    private Long workId;

    @NotNull(message = "课程计划ID不能为空")
    @ApiModelProperty(value = "课程计划ID", required = true)
    private Long teachplanId;
    
    @ApiModelProperty(value = "课程ID", required = false)
    private Long courseId;

    @NotEmpty(message = "提交内容不能为空")
    @ApiModelProperty(value = "提交内容", required = true)
    private String content;
}