package com.example.monghyang.domain.util;

import com.example.monghyang.domain.users.details.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    // 인증된 회원의 권한 정보를 확인하는 유틸 클래스

    public static Long getUserId() {
        // 인증 정보 중 회원 식별자 정보를 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 현재 인증정보
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal(); // 인증 정보 추출
        if (principal instanceof JwtUserDetails) {
            return ((JwtUserDetails) principal).getUserId(); // JwtUserDetails로 변환한 뒤 식별자 추출
        } else {
            return null;
        }
    }

    public static String getRole() {
        // 인증 정보 중 회원 권한(role) 정보를 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 현재 인증정보
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // 인증정보의 권한 중 첫번째 요소 반환(권한은 여러 개 가질 수 있으나 로직 상 1개로 제한했기 때문입니다)
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }
}
