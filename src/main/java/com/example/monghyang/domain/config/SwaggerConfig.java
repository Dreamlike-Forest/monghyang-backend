package com.example.monghyang.domain.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        // 1. 세션 ID를 위한 SecurityScheme 정의
        SecurityScheme sessionIdScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // API KEY 타입 사용
                .in(SecurityScheme.In.HEADER)     // 헤더에 위치
                .name("X-Session-Id");            // 헤더 이름

        // 2. 리프레시 토큰을 위한 SecurityScheme 정의
        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // API KEY 타입 사용
                .in(SecurityScheme.In.HEADER)
                .name("X-Refresh-Token");

        // 3. SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes("SessionID", sessionIdScheme)
                .addSecuritySchemes("RefreshToken", refreshTokenScheme);


        // 4. OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .info(apiInfo())
                .components(components);
    }
    private Info apiInfo() {
        return new Info()
                .title("몽향 API Doc") // API의 제목
                .description("몽향 프로젝트 API 문서(개발 중)") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }
}
