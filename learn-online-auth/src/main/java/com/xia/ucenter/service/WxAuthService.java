package com.xia.ucenter.service;

import com.xia.ucenter.model.po.XcUser;

import java.util.Map;

public interface WxAuthService {

    /**
     * 微信扫码登录
     * @param code
     * @return
     */
    public XcUser wxAuth(String code);

    /**
     * 微信扫码保存用户信息
     * @param userinfo
     * @return
     */
    XcUser addWxUser(Map<String, String> userinfo);
}
