package com.example.monghyang.domain.config;

import com.example.monghyang.domain.filter.JwtFilter;
import com.example.monghyang.domain.filter.LoginFilter;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.oauth2.service.CustomOAuth2UserService;
import com.example.monghyang.domain.users.service.UsersService;
import com.example.monghyang.domain.util.ExceptionUtil;
import com.example.monghyang.domain.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
    public LoginFilter loginFilter(AuthenticationManager authenticationManager, UsersService usersService, JwtUtil jwtUtil, ObjectMapper objectMapper) {
        LoginFilter loginFilter = new LoginFilter(authenticationManager, jwtUtil, usersService, objectMapper);
        loginFilter.setFilterProcessesUrl("/api/auth/login");
        loginFilter.setAuthenticationManager(authenticationManager); // loginFilter의 상위 부모 필터의 필드를 초기화하는 상위 클래스의 메소드
        return loginFilter;
    }

    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFilter loginFilter, JwtFilter jwtFilter, AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler, CustomAuthenticationEntryPoint authenticationEntryPoint, CustomAccessDeniedHandler accessDeniedHandler) throws Exception {

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

//                        // CORS 응답에서 노출될 헤더를 설정. 클라이언트에서 `Access`, 'Refresh' 헤더에 접근할 수 있도록 허용
//                        // List를 인자로 받는다.
//                        config.setExposedHeaders(List.of("Access", "Refresh"));
                        return config;
                    }
                }))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint) // 인증 실패 시 401 반환
                        .accessDeniedHandler(accessDeniedHandler) // 권한 부족 시 403 반환
                )
                .authorizeHttpRequests((auth) ->
                        auth.requestMatchers("/api/auth/**", "/oauth2/**","/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN") // Spring Security에서는 권한의 "ROLE_" 부분을 제외한 나머지 부분만 취급한다.
                                .requestMatchers("/api/brewery-control/**").hasAnyRole("ADMIN", "BREWERY")
                                .requestMatchers("/api/seller-control/**").hasAnyRole("ADMIN", "SELLER", "BREWERY")
                                .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login((oauth2) -> oauth2.loginPage("/auth/login")
                        .redirectionEndpoint(redirection -> redirection.baseUri("/oauth2/code/*"))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                )
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, LoginFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
