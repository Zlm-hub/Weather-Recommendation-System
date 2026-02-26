package com.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类 (终极版)
 * 核心：使用 @Order(Ordered.HIGHEST_PRECEDENCE) 强制排在 Spring Security 之前
 */
@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // ⚡️⚡️⚡️ 关键：设置为最高优先级
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 允许携带凭证 (Cookie, Token)
        config.setAllowCredentials(true);

        // 2. 允许所有来源 (解决跨域核心)
        config.addAllowedOriginPattern("*");

        // 3. 允许所有请求头 (Authorization, token, content-type...)
        config.addAllowedHeader("*");

        // 4. 允许所有请求方法 (GET, POST, OPTIONS...)
        config.addAllowedMethod("*");

        // 5. 注册配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}