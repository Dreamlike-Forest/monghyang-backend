package com.example.monghyang.domain.users.service;

import com.example.monghyang.domain.authHandler.CustomLogoutHandler;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.users.dto.ReqUsersDto;
import com.example.monghyang.domain.users.dto.ResUsersDto;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UsersService {
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 패스워드 암호화 모듈
    private final RoleRepository roleRepository;
    @Autowired
    public UsersService(UsersRepository usersRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder, CustomLogoutHandler customLogoutHandler) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResUsersDto getUsersByEmail(String email) {
        Users users = usersRepository.findByEmailActive(email).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        return ResUsersDto.usersJoinedWithRoleToDto(users);
    }

    @Transactional
    public void updateUsers(Long userId, ReqUsersDto reqUsersDto) {
        if(reqUsersDto == null) {
            throw new ApplicationException(ApplicationError.DTO_NULL_ERROR);
        }

        Users users = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));

        // 비밀번호 변경 시 인증 및 새 비밀번호 암호화
        if(reqUsersDto.getCurPassword() != null) {
            // 클라이언트가 보내온 '기존 비밀번호' 값과 DB에 저장된 값이 일치해야 한다.
            if(bCryptPasswordEncoder.matches(reqUsersDto.getCurPassword(), users.getPassword())) {
                // 비밀번호 변경 요청이 포함되어 있어야 한다.
                if(reqUsersDto.getNewPassword() != null) {
                    String newPassword = bCryptPasswordEncoder.encode(reqUsersDto.getNewPassword());
                    reqUsersDto.setNewPassword(newPassword);
                } else {
                    // 새 비밀번호 필드 비어있음을 알리는 예외
                    throw new ApplicationException(ApplicationError.NEW_PASSWORD_NULL);
                }

            } else {
                // 사용자가 입력한 '현재 비밀번호'가 DB와 일치하지 않음을 알리는 예외
                throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
            }
        }

        users.updateUsers(reqUsersDto);
    }
}
