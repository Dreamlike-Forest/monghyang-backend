package com.example.monghyang.domain.auth.service;

import com.example.monghyang.domain.auth.details.LoginUserDetails;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;
    @Autowired
    public CustomUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override // 이메일을 통해 유저를 조회한 결과를 AuthDto 인스턴스에 담고, LoginUserDetails 생성자의 매개변수로 담아 반환
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmailActiveJoinedRole(email).orElseThrow(() ->
                new UsernameNotFoundException("아이디와 비밀번호가 일치하지 않습니다."));

        return LoginUserDetails.builder()
                .userId(user.getId())
                .roleType(user.getRole().getName().toString())
                .nickname(user.getNickname())
                .password(user.getPassword()).build();
    }
}
