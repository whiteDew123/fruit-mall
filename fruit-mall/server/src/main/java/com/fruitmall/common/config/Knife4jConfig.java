package com.fruitmall.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置（Knife4j + OpenAPI 3）。
 * 按接口前缀分组，消费者端与商家后台分开，访问地址 /doc.html。
 * 接口文档由配置与注解自动生成，不手写（见 docs/01-开发规范.md 第 6.3 节）。
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI fruitMallOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("基于推荐算法的水果商城 —— 接口文档")
                .description("消费者端 H5 与商家后台共用的后端服务接口")
                .version("v0.1.0")
                .contact(new Contact().name("毕业设计")));
    }

    /** 消费者端：/api/shop/** */
    @Bean
    public GroupedOpenApi shopApi() {
        return GroupedOpenApi.builder()
                .group("消费者端")
                .pathsToMatch("/api/shop/**")
                .build();
    }

    /** 商家后台：/api/admin/** */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("商家后台")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    /** 登录注册：/api/auth/** */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("登录注册")
                .pathsToMatch("/api/auth/**")
                .build();
    }
}
