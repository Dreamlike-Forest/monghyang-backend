package com.example.monghyang.domain.users.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.users.dto.JoinDto;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import com.example.monghyang.domain.util.JwtUtil;
import com.example.monghyang.domain.util.SessionUtil;
import com.example.monghyang.domain.util.dto.JwtClaimsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UsersService {
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 암호화
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final SessionUtil sessionUtil;

    @Autowired
    public UsersService(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleRepository roleRepository, JwtUtil jwtUtil
        , RedisService redisService, SessionUtil sessionUtil) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
        this.sessionUtil = sessionUtil;
    }

    // RT을 이용한 세션 및 RT 갱신
    @Transactional
    public void updateRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 토큰에서 userid, devicetype 추출해서 세션 및 토큰 갱신에 사용
        String refreshToken = request.getHeader("X-Refresh-Token");

        JwtClaimsDto jwtClaimsDto = jwtUtil.parseRefreshToken(refreshToken);
        Long userId = jwtClaimsDto.getUserId();
        String deviceType = jwtClaimsDto.getDeviceType();
        String tid = jwtClaimsDto.getTid();
        String role = jwtClaimsDto.getRole();

        if(!redisService.verifyRefreshTokenTid(userId, deviceType, tid)) {
            // 동시접속 감지 기준: 해당 유저의 해당 디바이스 타입의 리프레시 토큰 tid와 요청에 담긴 RT의 tid가 동일하지 않은 경우
            throw new ApplicationException(ApplicationError.CONCURRENT_CONNECTION);
        }

        // 세션 및 토큰 갱신
        sessionUtil.createNewAuthInfo(request, response, userId, role);
    }

    @Transactional
    public void commonJoin(JoinDto joinDto) {
        // 약관에 동의하지 않으면 회원가입 불가
        if(joinDto.getIsAgreed().equals(Boolean.FALSE)) {
            throw new ApplicationException(ApplicationError.TERMS_AND_CONDITIONS_NOT_AGREED);
        }

        Role role = roleRepository.findByName(RoleType.ROLE_USER).orElseThrow(() ->
                new ApplicationException(ApplicationError.ROLE_NOT_FOUND));

        String password = bCryptPasswordEncoder.encode(joinDto.getPassword()); // 비밀번호 해싱
        Boolean gender = (joinDto.getGender().equals("man")) ? Boolean.FALSE : Boolean.TRUE; // false: 남자, true: 여자

        Users users = Users.generalBuilder().email(joinDto.getEmail())
                .password(password)
                .role(role)
                .nickname(joinDto.getNickname()).name(joinDto.getName())
                .phone(joinDto.getPhone()).birth(joinDto.getBirth())
                .gender(gender).address(joinDto.getAddress())
                .address_detail(joinDto.getAddress_detail())
                .isAgreed(joinDto.getIsAgreed()).build();
        usersRepository.save(users);
    }

}
