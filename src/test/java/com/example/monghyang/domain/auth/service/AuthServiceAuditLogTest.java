package com.example.monghyang.domain.auth.service;

import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.logging.AuditLogger;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.seller.repository.SellerImageRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import com.example.monghyang.domain.util.JwtUtil;
import com.example.monghyang.domain.util.SessionUtil;
import com.example.monghyang.domain.util.dto.JwtClaimsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceAuditLogTest {
    @Mock UsersRepository usersRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock RoleRepository roleRepository;
    @Mock JwtUtil jwtUtil;
    @Mock RedisService redisService;
    @Mock SessionUtil sessionUtil;
    @Mock SellerRepository sellerRepository;
    @Mock BreweryRepository breweryRepository;
    @Mock RegionTypeRepository regionTypeRepository;
    @Mock StorageService storageService;
    @Mock BreweryImageRepository breweryImageRepository;
    @Mock SellerImageRepository sellerImageRepository;
    @Mock BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    @Mock BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @Mock AuditLogger auditLogger;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                usersRepository,
                passwordEncoder,
                roleRepository,
                jwtUtil,
                redisService,
                sessionUtil,
                sellerRepository,
                breweryRepository,
                regionTypeRepository,
                storageService,
                breweryImageRepository,
                sellerImageRepository,
                breweryWeeklyOpenTimeRepository,
                breweryWeeklyBreakTimeRepository,
                Clock.systemDefaultZone(),
                auditLogger
        );
    }

    @Test
    @DisplayName("토큰 갱신 성공은 성공 감사 로그를 남긴다")
    void token_refresh_success_writes_audit_log() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Refresh-Token", "masked");
        JwtClaimsDto claims = JwtClaimsDto.tidUserIdDeviceTypeRoleOf("tid", 1L, "ROLE_USER");
        given(jwtUtil.parseRefreshToken("masked")).willReturn(claims);

        authService.updateRefreshToken(request, response);

        verify(redisService).deleteRefreshTokenAndSession(1L, "tid");
        verify(sessionUtil).createNewAuthInfo(request, response, 1L, "ROLE_USER");
        verify(auditLogger).logSecuritySuccess("TOKEN_REFRESH_SUCCESS", request, 1L, "ROLE_USER");
    }
}
