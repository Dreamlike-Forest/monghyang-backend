package com.example.monghyang.domain.oauth2.handler;

import com.example.monghyang.domain.oauth2.details.CustomOAuth2UserDetails;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final String clientUrl;

    public CustomOAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, @Value("${app.client-url}") String clientUrl) {
        this.jwtUtil = jwtUtil;
        this.clientUrl = clientUrl;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        CustomOAuth2UserDetails customOAuth2UserDetails = (CustomOAuth2UserDetails) oAuth2User;
        String oAuth2Id = customOAuth2UserDetails.getOAuth2Id();
        Users user = customOAuth2UserDetails.getUser();

        // 가입절차가 완료되지 않은 경우 분기처리(전화번호 컬럼 값 존재 유무로 판단)
        if(user.getPhone() == null) {
            request.getSession().setAttribute("incompleteUserId", user.getId());
            response.sendRedirect(clientUrl + "/auth/complete"); // 나머지 유저 정보를 입력하는 창으로 리다이렉션
        } else {
            Long userId = user.getId();
            Collection<? extends GrantedAuthority> authorities = customOAuth2UserDetails.getAuthorities();

            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority grantedAuthority = iterator.next();
            String role = grantedAuthority.getAuthority();

            ResponseCookie accessCookie = jwtUtil.createAccessToken(userId, role);
            ResponseCookie refreshCookie = jwtUtil.createRefreshToken(userId, role);

//            response.addCookie(accessCookie);
//            response.addCookie(refreshCookie);
            response.setHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            response.sendRedirect(clientUrl);
        }
    }
}
