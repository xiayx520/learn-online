package com.xia.learning.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学习排行榜项目VO
 */
@Data
@ApiModel(value="LearnRankItemVO", description="学习排行榜项目VO")
public class LearnRankItemVO {
    
    @ApiModelProperty(value = "用户ID")
    private String userId;
    
    @ApiModelProperty(value = "用户名")
    private String userName;
    
    @ApiModelProperty(value = "排行值(可以是学习时长或完成率)")
    private Double value;
} 