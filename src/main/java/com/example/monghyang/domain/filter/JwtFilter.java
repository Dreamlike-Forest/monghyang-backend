package com.example.monghyang.domain.filter;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.users.details.JwtUserDetails;
import com.example.monghyang.domain.users.dto.AuthDto;
import com.example.monghyang.domain.util.ExceptionUtil;
import com.example.monghyang.domain.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false; // Async-Dispatch 에도 필터를 수행하여 HTTP 헤더를 파싱하도록 설정
    }


    @Override // access token을 검증하는 메소드. refresh token에 대한 검증 요청은 AuthController가 처리
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = null;

        // 요청에 쿠키가 존재하는지 확인
        if(request.getCookies() != null) {
            // 쿠키에서 access token을 찾는 과정
            for(Cookie cookie : request.getCookies()) {
                if(cookie.getName().equals("access_token")) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        // access token을 발견하지 못한 경우, 다음 필터로 요청을 넘긴다: 로그인하는 경우
        if(accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 만료된 경우
        if(jwtUtil.isExpired(accessToken)) {
            filterChain.doFilter(request, response);
//            response.sendRedirect(request.getContextPath() + "/api/user/refresh"); // refresh token을 이용한 토큰 재발급 요청을 하도록 리다이렉션 응답
            ExceptionUtil.filterExceptionHandler(response, ApplicationError.ACCESS_TOKEN_EXPIRED);
            return;
        }

        // 토큰에서 유저 식별자와 권한 정보 추출
        Long userId = jwtUtil.getUserId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        // 추출한 정보를 AuthDto에 담는다.
        AuthDto authDto = new AuthDto();
        authDto.setUserId(userId);
        authDto.setRoleType(role);
        authDto.setPassword("");

        // AuthDto를 JwtUserDetails에 담아 인증 토큰을 생성하기 위한 준비물을 만든다.
        JwtUserDetails jwtUserDetails = new JwtUserDetails(authDto);
        // 인증토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(jwtUserDetails, null, jwtUserDetails.getAuthorities());

        // Security Context Holder 임시 세션에 인증토큰 정보를 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
