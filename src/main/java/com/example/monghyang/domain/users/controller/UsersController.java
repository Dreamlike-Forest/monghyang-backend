package com.example.monghyang.domain.users.controller;

import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.users.dto.ResUsersDto;
import com.example.monghyang.domain.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "회원 공통 API")
public class UsersController {
    private final UsersService usersService;
    @Autowired
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Email로 회원을 조회합니다.")
    public ResponseEntity<ResponseDataDto<ResUsersDto>> getUsersByEmail(@PathVariable String email) {
        ResUsersDto resUsersDto = usersService.getUsersByEmail(email);
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(resUsersDto));
    }

    // 회원 정보 수정 api


    // 회원 탈퇴 api
}
