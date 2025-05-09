package com.xia.ucenter.service;

import java.util.List;
import java.util.Map;

public interface UserInfoService {

    /**
     * 根据用户id查询用户名称
     * @param ids
     * @return
     */
    Map getNameByIds(List<String> ids);
}
