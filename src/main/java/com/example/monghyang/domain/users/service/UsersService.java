package com.example.monghyang.domain.users.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.users.dto.ResUsersDto;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsersService {
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    @Autowired
    public UsersService(UsersRepository usersRepository, RoleRepository roleRepository) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
    }

    public ResUsersDto getUsersByEmail(String email) {
        Users users = usersRepository.findByEmail(email).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));

        return ResUsersDto.usersFetchJoinedRoleFrom(users);
    }
}
