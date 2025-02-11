package com.xia.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    @ApiModelProperty("数据列表")
    // 数据列表
    private List<T> items;
    @ApiModelProperty("总记录数")
    //总记录数
    private Long counts;
    @ApiModelProperty("当前页码")
    //当前页码
    private Long page;
    @ApiModelProperty("每页记录数")
    //每页记录数
    private Long pageSize;
}
