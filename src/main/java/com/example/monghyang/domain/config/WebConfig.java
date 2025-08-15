package com.example.monghyang.domain.config;

import com.example.monghyang.domain.authHandler.LoginUserIdArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginUserIdArgumentResolver loginUserIdArgumentResolver;
    @Autowired
    public WebConfig(LoginUserIdArgumentResolver loginUserIdArgumentResolver) {
        this.loginUserIdArgumentResolver = loginUserIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // LoginUserIdArgumentResolver를 Spring MVC가 사용하도록 설정
        resolvers.add(loginUserIdArgumentResolver);
    }

}
