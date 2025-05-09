package com.xia.learning.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable throwable) {

        return new UserServiceClient() {
            @Override
            public Map<String, String> getUserName(List<String> ids) {
                log.error("远程调用用户服务熔断异常：{}", throwable.getMessage());
                return Collections.emptyMap();
            }
        };
    }
}
