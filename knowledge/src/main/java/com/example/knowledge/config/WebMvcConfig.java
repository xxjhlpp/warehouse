package com.example.knowledge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${upload.base-path}")
    private String uploadBasePath;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 关键配置：将URL中的 /avatars/** 映射到本地 D:/app-uploads/avatar/ 目录
        registry.addResourceHandler("/avatars/**")  // 前端访问的URL路径
                .addResourceLocations("file:D:/app-uploads/avatar/");  // 实际文件存储路径


        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBasePath);
   }
}