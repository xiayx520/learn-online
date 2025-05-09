package com.xia.learning.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@FeignClient(value = "auth-service", fallbackFactory = UserServiceClientFallbackFactory.class)
@RequestMapping("/auth")
public interface UserServiceClient {

    @GetMapping("/user/name/{ids}")
    public Map<String, String> getUserName(@PathVariable("ids") List<String> ids);
}
