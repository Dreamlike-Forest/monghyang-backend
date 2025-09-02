package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.util.DeviceTypeUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Component
@Slf4j
public class CustomSecurityContextRepository implements SecurityContextRepository {

    private static final String SESSION_HEADER_NAME = "X-Session-Id";
    private static final String SESSION_USER_INFO_NAME = "sessionUserInfo";
    private final RedisService redisService;


    @Autowired
    public CustomSecurityContextRepository(RedisService redisService) {
        this.redisService = redisService;
    }


    // IDE의 컴파일 에러를 해결하기 위한 임시방편 코드입니다.
    @Override
    @Deprecated
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return loadDeferredContext(requestResponseHolder.getRequest()).get();
    }

    @Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
        // supplier: 세션 조회를 어떻게 하고, 불러온 세션의 구조는 어떻게 생겼는지에 대한 일종의 설명서 객체
        Supplier<SecurityContext> supplier = () -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            HttpSession session = request.getSession(false); // 세션 저장소에서 SID로 세션 조회
            if(session == null) {
                // 조회 결과 없다면 '익명' 사용자
                return context;
            }

            Object sessionUserInfo = session.getAttribute(SESSION_USER_INFO_NAME);
            if(sessionUserInfo instanceof SessionUserInfo(Long userId, String role)) {
                // 세션에 저장된 SessionUserInfo 객체 파싱
                String deviceType = DeviceTypeUtil.getDeviceType(request).name();
                List<GrantedAuthority> authentication = Collections.singletonList(new SimpleGrantedAuthority(role));

                redisService.expireLoginInfo(userId, deviceType); // redis의 로그인 정보 ttl 또한 갱신

                // 인증 정보 생성 및 세팅
                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authentication);
                context.setAuthentication(auth);
            } else {
                log.error("세션 정보가 SessionUserInfo 레코드 형식이 아닙니다.");
            }
            return context;

        };
        return new CustomDeferredSecurityContext(supplier);
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        // security context을 별도로 갱신하지 않으므로 비워뒀습니다.
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        // 요청 헤더에 SID가 있는지 확인
        return request.getHeader(SESSION_HEADER_NAME) != null;
    }

    private static class CustomDeferredSecurityContext implements DeferredSecurityContext {
        private final Supplier<SecurityContext> supplier;
        private SecurityContext securityContext;
        public CustomDeferredSecurityContext(Supplier<SecurityContext> supplier) {
            this.supplier = supplier;
        }

        @Override
        public boolean isGenerated() {
            // security context가 실제로 로드되었는지 판단
            return this.securityContext != null;
        }

        @Override
        public SecurityContext get() {
            // supplier를 통해 SecurityContext를 실제로 로드하는 메소드
            // 아직 로드되지 않았다면 supplier을 통해 로드, 결과 캐싱
            if(this.securityContext == null) {
                this.securityContext = this.supplier.get();
            }
            return this.securityContext;
        }
    }
}
