package com.example.monghyang.domain.filter;

import com.example.monghyang.domain.authHandler.SessionUserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {
    private final WebAuthenticationDetailsSource detailsSource = new WebAuthenticationDetailsSource();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication existedAuth = SecurityContextHolder.getContext().getAuthentication();
            // 기존의 인증 토큰이 존재하지 않거나, 존재하더라도 '익명 인증 토큰'인 경우에만 새로운 인증 정보를 생성
            if(existedAuth == null || existedAuth instanceof AnonymousAuthenticationToken) {
                HttpSession session = request.getSession(false);
                if(session != null) {
                    SessionUserInfo sessionUserInfo = (SessionUserInfo) session.getAttribute("sessionUserInfo");

                    if(sessionUserInfo != null && sessionUserInfo.userId() != null && sessionUserInfo.role() != null) {
                        List<GrantedAuthority> authentication = Collections.singletonList((new SimpleGrantedAuthority(sessionUserInfo.role())));

                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(sessionUserInfo.userId(), null, authentication);
                        authenticationToken.setDetails(detailsSource.buildDetails(request));

                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        context.setAuthentication(authenticationToken);
                        SecurityContextHolder.setContext(context);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
