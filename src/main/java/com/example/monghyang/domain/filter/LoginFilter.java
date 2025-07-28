package com.example.monghyang.domain.filter;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.users.details.LoginUserDetails;
import com.example.monghyang.domain.users.service.UsersService;
import com.example.monghyang.domain.util.ExceptionUtil;
import com.example.monghyang.domain.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UsersService usersService;
    @Value("${app.client-url}")
    private String clientUrl;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UsersService usersService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.usersService = usersService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if(email == null || password == null) {
            ExceptionUtil.filterExceptionHandler(response, ApplicationError.USER_BAD_REQUEST);
        }
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password, null);
        return authenticationManager.authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) {
        LoginUserDetails loginUserDetails = (LoginUserDetails) auth.getPrincipal();

        Long userId = loginUserDetails.getUserId();

        Collection<? extends GrantedAuthority> authorities = loginUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority grantedAuthority = iterator.next();
        String role = grantedAuthority.getAuthority();

        String refreshToken = jwtUtil.createRefreshToken(userId, role);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshToken);

        // 이미 로그인 시 유저 정보를 검증했으니, 이 메소드에서는 '업데이트' 쿼리만 호출한다.
//        usersService.setRefreshToken(userId, refreshToken);

        try{
            response.setContentType("application/json;charset=utf-8");
            objectMapper.writeValue(response.getWriter(), LoginDto.nicknameRoleOf(loginUserDetails.getUsername(), role));
        } catch (IOException e) {
            log.error(e.getMessage() + "\n 로그인 성공 후 응답할 http body 생성 중 에러가 발생했습니다.");
//            ExceptionUtil.filterExceptionHandler(response, ApplicationError.LOGIN_RESPONSE_ERROR);
        }
    }

    @Override // 로그인 실패 시 수행되는 메소드
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed) {
        ExceptionUtil.filterExceptionHandler(res, ApplicationError.USER_UNAUTHORIZED);
    }

}
