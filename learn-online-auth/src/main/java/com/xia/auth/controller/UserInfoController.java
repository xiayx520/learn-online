package com.xia.auth.controller;

import com.xia.ucenter.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Api(value = "用户管理接口", tags = "用户管理接口")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;


    /**
     * 根据用户ids获取用户学名
     * @param ids
     * @return
     */
    @ApiOperation("根据用户id获取用户学名")
    @GetMapping("/user/name/{ids}")
    public Map<String, String> getUserName(@PathVariable("ids") List<String> ids) {
        log.info("根据用户ids获取用户学名，用户ids：{}", ids);
        return userInfoService.getNameByIds(ids);
    }
}
