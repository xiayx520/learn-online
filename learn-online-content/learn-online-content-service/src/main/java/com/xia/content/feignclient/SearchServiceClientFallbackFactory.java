package com.xia.content.feignclient;

import com.xia.content.model.po.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {

        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.debug("调用搜索服务添加课程信息时发生熔断，异常信息:{}", throwable.toString(), throwable);
                return false;
            }
        };
    }
}
