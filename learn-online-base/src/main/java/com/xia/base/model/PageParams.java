package com.xia.base.model;

import lombok.Data;

@Data
public class PageParams {

    //当前页码
    private Long pageNo = 1L;

    //每页记录数默认值
    private Long pageSize =10L;

}
