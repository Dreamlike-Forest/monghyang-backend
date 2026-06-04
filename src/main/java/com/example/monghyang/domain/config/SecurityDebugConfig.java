package com.example.monghyang.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * prod 환경을 제외한 프로파일에서만 '@EnableWebSecurity' 디버그 모드 활성화하기 위한 config bean
 */
@Configuration
@Profile("!prod")
@EnableWebSecurity(debug = true)
public class SecurityDebugConfig {
}