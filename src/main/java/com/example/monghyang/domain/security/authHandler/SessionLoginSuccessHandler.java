package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.security.filter.LoginDto;
import com.example.monghyang.domain.auth.details.LoginUserDetails;
import com.example.monghyang.domain.util.SessionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class SessionLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final SessionUtil sessionUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 인증 객체에서 로그인 유저 정보 추출
        LoginUserDetails loginUserDetails = (LoginUserDetails) authentication.getPrincipal();
        String nickname = loginUserDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = loginUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        String role = iterator.next().getAuthority();
        Long userId = loginUserDetails.getUserId();

        // 새로운 세션 & 리프레시 토큰 생성
        sessionUtil.createNewAuthInfo(request, response, userId, role);

        // 응답 http body 작성
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), LoginDto.nicknameRoleOf(nickname, role)); // 유저 닉네임, 권한 정보 반환(json)
    }
}
