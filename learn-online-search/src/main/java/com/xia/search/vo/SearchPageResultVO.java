package com.xia.search.vo;


import com.xia.base.model.PageResult;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/25 17:51
 */
@Data
@ToString
public class SearchPageResultVO<T> extends PageResult {

    //大分类列表
    List<String> mtList;
    //小分类列表
    List<String> stList;

    public SearchPageResultVO(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }

}
