package com.example.monghyang.domain.security.config;

import com.example.monghyang.domain.global.annotation.auth.LoginUserIdArgumentResolver;
import com.example.monghyang.domain.global.annotation.auth.LoginUserRoleArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginUserIdArgumentResolver loginUserIdArgumentResolver;
    private final LoginUserRoleArgumentResolver loginUserRoleArgumentResolver;

    @Autowired
    public WebConfig(LoginUserIdArgumentResolver loginUserIdArgumentResolver, LoginUserRoleArgumentResolver loginUserRoleArgumentResolver) {
        this.loginUserIdArgumentResolver = loginUserIdArgumentResolver;
        this.loginUserRoleArgumentResolver = loginUserRoleArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 커스텀 어노테이션을 Spring MVC가 사용하도록 설정
        resolvers.add(loginUserIdArgumentResolver);
        resolvers.add(loginUserRoleArgumentResolver);
    }

}
