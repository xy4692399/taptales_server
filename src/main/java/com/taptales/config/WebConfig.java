package com.taptales.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/wechatLogin")
                .excludePathPatterns("/api/shop/owner/login")
                .excludePathPatterns("/api/admin/login")
                .excludePathPatterns("/api/menu/all")
                .excludePathPatterns("/api/menu/shop/**")
                .excludePathPatterns("/api/menu/drinks/**")
                .excludePathPatterns("/api/activity/public/list")
                .excludePathPatterns("/api/activity/public/shop/**")
                .excludePathPatterns("/api/ranking/leaderboard");
    }
}
