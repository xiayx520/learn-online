package com.xia.base.model;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    // 数据列表
    private List<T> items;

    //总记录数
    private Long counts;

    //当前页码
    private Long page;

    //每页记录数
    private Long pageSize;
}
