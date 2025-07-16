package com.example.monghyang.domain.users.service;

import com.example.monghyang.domain.users.details.JwtUserDetails;
import com.example.monghyang.domain.users.details.LoginUserDetails;
import com.example.monghyang.domain.users.dto.AuthDto;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        Optional<Users> user = usersRepository.findByEmail(email);

        if(user.isPresent()) {
            AuthDto authDto = new AuthDto();
            authDto.setUserId(user.get().getId());
            authDto.setRoleType(user.get().getRole().getName().toString());
            return new LoginUserDetails(authDto, user.get().getNickname(), user.get().getPassword());
        } else {
            return null;
        }
    }
}
