package com.example.knowledge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端域名访问（与前端地址保持一致）
        config.addAllowedOrigin("http://localhost:8081");
        // 允许携带cookie
        config.setAllowCredentials(true);
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 预检请求有效期
        config.setMaxAge(3600L);
        // 允许前端访问的响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Set-Cookie");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有接口生效
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}