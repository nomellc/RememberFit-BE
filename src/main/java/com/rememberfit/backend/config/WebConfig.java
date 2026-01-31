package com.rememberfit.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 주소에 대해
                .allowedOrigins("*") // 모든 출처(기기, 웹사이트)를 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE"); // 이 방식들 허용
    }
}
