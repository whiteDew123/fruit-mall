package com.fruitmall.common.config;

import com.fruitmall.common.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web MVC 配置。
 * 包含跨域配置（供本地开发时两个前端工程调用）与登录态、权限校验拦截器的注册。
 * 白名单接口在 application.yml 的 fruit-mall.auth.exclude-paths 中集中维护，
 * 新增公开接口（如商品浏览、推荐）只改配置，不改代码。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /** 免登录接口白名单，逗号分隔 */
    @Value("${fruit-mall.auth.exclude-paths:}")
    private String excludePaths;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludes = StringUtils.hasText(excludePaths)
                ? Arrays.stream(excludePaths.split(",")).map(String::trim).filter(StringUtils::hasText).toList()
                : List.of();
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(excludes);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 本地开发放开来源；部署到服务器时应收紧为实际域名
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
