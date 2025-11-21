package com.example.monghyang.domain.security.config;

import com.example.monghyang.domain.security.filter.ExceptionHandlerFilter;
import com.example.monghyang.domain.oauth2.handler.CustomOAuth2AuthenticationFailureHandler;
import com.example.monghyang.domain.oauth2.handler.CustomOAuth2AuthenticationSuccessHandler;
import com.example.monghyang.domain.oauth2.service.CustomOAuth2UserService;
import com.example.monghyang.domain.security.authHandler.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfig {
    private final List<String> clientUrl;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSecurityContextRepository customSecurityContextRepository;

    @Autowired
    public SecurityConfig(@Value("${app.client-url}") List<String> clientUrl, CustomOAuth2UserService customOAuth2UserService, CustomSecurityContextRepository customSecurityContextRepository) {
        this.clientUrl = clientUrl;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSecurityContextRepository = customSecurityContextRepository;
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        // actuator 요청에 대한 Security Filter Chain
        http
                .securityContext(c -> c // security context를 세션에 저장하지 않는 설정
                        .securityContextRepository(customSecurityContextRepository)
                        .requireExplicitSave(true))
                .requestCache(AbstractHttpConfigurer::disable) // 요청에 대한 캐시 비활성화
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/actuator/health").permitAll()
                                .anyRequest().hasRole("ADMIN"));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint authenticationEntryPoint, CustomAccessDeniedHandler accessDeniedHandler, CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler, CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler, SessionLoginSuccessHandler sessionLoginSuccessHandler, SessionLoginFailureHandler sessionLoginFailureHandler, CustomLogoutHandler customLogoutHandler, SessionLogoutSeccessHandler sessionLogoutSeccessHandler, ExceptionHandlerFilter exceptionHandlerFilter) throws Exception {
        // application api 요청에 대한 Security Filter Chain
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors((corsCustom) -> corsCustom.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        // 허용할 Origin 설정
                        config.setAllowedOrigins(clientUrl);

                        // 허용할 HTTP 메서드 설정 ("*": 모든 HTTP 메소드)
                        config.setAllowedMethods(Collections.singletonList("*"));

                        // 브라우저에서 JS로 접근할 수 있는 커스텀 헤더의 목록 정의
                        config.setExposedHeaders(List.of("X-Session-Id", "X-Refresh-Token"));

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
                        .securityContextRepository(customSecurityContextRepository)
                        .requireExplicitSave(true))
                .requestCache(AbstractHttpConfigurer::disable) // 요청에 대한 캐시 비활성화
                .exceptionHandling(ex -> ex // 인증 및 권한 검증 시 발생 예외 처리
                        .authenticationEntryPoint(authenticationEntryPoint) // 세션 조회 실패 시 404 반환
                        .accessDeniedHandler(accessDeniedHandler) // 권한 부족 시 403 반환
                )
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Spring Security에서는 권한의 "ROLE_" 부분을 제외한 나머지 부분만 취급한다.
                        .requestMatchers("/api/brewery-priv/**").hasAnyRole("ADMIN", "BREWERY")
                        .requestMatchers("/api/seller-priv/**").hasAnyRole("ADMIN", "SELLER", "BREWERY")
                        .requestMatchers("/api/auth/**", "/oauth2/**","/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/actuator/**" ,"/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form // form 로그인
//                        .loginPage(clientUrl + "/?view=login")
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
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }
}
