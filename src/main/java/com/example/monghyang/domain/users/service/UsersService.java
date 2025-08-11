package com.example.monghyang.domain.users.service;

import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import com.example.monghyang.domain.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsersService {
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final SessionUtil sessionUtil;

    @Autowired
    public UsersService(UsersRepository usersRepository, RoleRepository roleRepository, SessionUtil sessionUtil) {
        this.usersRepository = usersRepository;
        this.roleRepository = roleRepository;
        this.sessionUtil = sessionUtil;
    }

}
