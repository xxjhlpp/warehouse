package com.example.knowledge.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Spring Boot 后端配置示例
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 允许访问uploads目录下的静态资源
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}