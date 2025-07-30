package com.example.monghyang.domain.users.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.dto.JoinDto;
import com.example.monghyang.domain.users.entity.Role;
import com.example.monghyang.domain.users.entity.RoleType;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import com.example.monghyang.domain.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    public UsersService(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleRepository roleRepository, JwtUtil jwtUtil) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }

    // refresh token을 이용한 token 갱신
//    @Transactional
//    public void updateRefreshToken(HttpServletRequest request, HttpServletResponse response) {
//        Long userId = AuthUtil.getUserId();
//        String userRole = AuthUtil.getRole();
//        if(userId == null) {
//            throw new ApplicationException(ApplicationError.AUTH_INFO_NOT_FOUND);
//        }
//
//        Users users = usersRepository.findById(userId).orElseThrow(() ->
//                new ApplicationException(ApplicationError.USER_NOT_FOUND));
//
//        if(jwtUtil.verifyRefreshToken(request)) {
//            // 두 토큰 새로 갱신
//            String refreshToken = jwtUtil.createRefreshToken(userId, userRole);
//            response.addHeader(HttpHeaders.SET_COOKIE, refreshToken);
////            setRefreshToken(userId, refreshToken);
//        }
//    }

    public void logout(HttpServletRequest request) {

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
