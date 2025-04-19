package com.xia;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages={"com.xia.content.feignclient"})
@SpringBootApplication
public class ContentApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ContentApplication.class, args);
    }
}
