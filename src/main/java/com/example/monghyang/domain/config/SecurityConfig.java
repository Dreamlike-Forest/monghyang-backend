package com.example.monghyang.domain.config;

import com.example.monghyang.domain.authHandler.*;
import com.example.monghyang.domain.filter.ExceptionHandlerFilter;
import com.example.monghyang.domain.filter.SessionAuthFilter;
import com.example.monghyang.domain.oauth2.handler.CustomOAuth2AuthenticationFailureHandler;
import com.example.monghyang.domain.oauth2.handler.CustomOAuth2AuthenticationSuccessHandler;
import com.example.monghyang.domain.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
public class SecurityConfig {
    private final String clientUrl;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(@Value("${app.client-url}") String clientUrl, CustomOAuth2UserService customOAuth2UserService) {
        this.clientUrl = clientUrl;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint authenticationEntryPoint, CustomAccessDeniedHandler accessDeniedHandler, CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler, CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler, SessionLoginSuccessHandler sessionLoginSuccessHandler, SessionLoginFailureHandler sessionLoginFailureHandler, SessionAuthFilter sessionAuthFilter, CustomLogoutHandler customLogoutHandler, SessionLogoutSeccessHandler sessionLogoutSeccessHandler, ExceptionHandlerFilter exceptionHandlerFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors((corsCustom) -> corsCustom.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        // 허용할 Origin 설정
                        // Collections.singletonList() == 단 하나의 요소만 가지는 불변(final) 리스트 생성
                        config.setAllowedOrigins(Collections.singletonList(clientUrl));

                        // 허용할 HTTP 메서드 설정 ("*": 모든 HTTP 메소드)
                        config.setAllowedMethods(Collections.singletonList("*"));

                        // 요청에 자격 증명(Credentials, 예: 쿠키, HTTP 인증 등)을 포함하도록 허용
                        // 이 설정이 true일 경우, `Access-Control-Allow-Credentials` 헤더가 true
                        config.setAllowCredentials(true);

                        // 허용할 요청 헤더 설정
                        config.setAllowedHeaders(Collections.singletonList("*"));

                        // CORS 응답 캐시 시간(초)을 설정 클라이언트가 얼마나 오랫동안 이 CORS 정책을 캐시할지를 설정
                        // 3600초(1시간)
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .securityContext(c -> c // security context를 세션에 저장하지 않는 설정
                        .securityContextRepository(new RequestAttributeSecurityContextRepository())
                        .requireExplicitSave(true))
                .requestCache(AbstractHttpConfigurer::disable) // 요청에 대한 캐시 비활성화
                .exceptionHandling(ex -> ex // 인증 및 권한 검증 시 발생 예외 처리
                        .authenticationEntryPoint(authenticationEntryPoint) // 세션 조회 실패 시 404 반환
                        .accessDeniedHandler(accessDeniedHandler) // 권한 부족 시 403 반환
                )
                .authorizeHttpRequests((auth) ->
                        auth.requestMatchers("/oauth2/**","/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/error").permitAll()
                                .requestMatchers("/api/auth/**", "/api/brewery/**", "/api/product/**").permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Spring Security에서는 권한의 "ROLE_" 부분을 제외한 나머지 부분만 취급한다.
                                .requestMatchers("/api/brewery-priv/**").hasAnyRole("ADMIN", "BREWERY")
                                .requestMatchers("/api/seller-priv/**").hasAnyRole("ADMIN", "SELLER", "BREWERY")
                                .anyRequest().authenticated())
                .formLogin(form -> form // form 로그인
                        .loginPage(clientUrl + "/?view=login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(sessionLoginSuccessHandler)
                        .failureHandler(sessionLoginFailureHandler))
                .oauth2Login((oauth2) -> oauth2.loginPage("/auth/login") // OAuth2 로그인
                        .redirectionEndpoint(redirection -> redirection.baseUri("/oauth2/code/*"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(customOAuth2AuthenticationSuccessHandler)
                        .failureHandler(customOAuth2AuthenticationFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler(sessionLogoutSeccessHandler)
                        .clearAuthentication(true)) // 현재 Security Context 비우기
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(sessionAuthFilter, LogoutFilter.class) // 로그아웃 필터 앞단에 세션 필터 삽입(즉, 인증 필터 중 제일 앞)
                .addFilterBefore(exceptionHandlerFilter, SessionAuthFilter.class) // 예외처리 필터. 가장 앞단에 위치
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }
}
