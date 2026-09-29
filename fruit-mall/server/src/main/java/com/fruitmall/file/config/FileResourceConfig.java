package com.fruitmall.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 上传目录的静态资源映射。
 * 把 /api/files/** 映射到本地上传目录，使上传后的图片可直接通过 URL 访问。
 */
@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final String accessPrefix;

    public FileResourceConfig(@Value("${fruit-mall.file.upload-dir:./upload}") String uploadDir,
                              @Value("${fruit-mall.file.access-prefix:/api/files}") String accessPrefix) {
        this.uploadDir = uploadDir;
        this.accessPrefix = accessPrefix;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(accessPrefix + "/**").addResourceLocations(location);
    }
}
