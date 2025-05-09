package com.xia.ucenter.service.impl;

import com.xia.ucenter.mapper.XcUserMapper;
import com.xia.ucenter.model.po.XcUser;
import com.xia.ucenter.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Override
    public Map getNameByIds(List<String> ids) {
        List<XcUser> xcUsers = xcUserMapper.selectBatchIds(ids);

        return xcUsers.stream().collect(Collectors.toMap(XcUser::getId, XcUser::getName));
    }
}
