package com.example.monghyang.domain.users.controller;

import com.example.monghyang.domain.global.response.ResponseDto;
import com.example.monghyang.domain.users.dto.JoinDto;
import com.example.monghyang.domain.users.service.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsersService usersService;
    @Autowired
    public AuthController(UsersService usersService) {
        this.usersService = usersService;
    }

//    @PostMapping("/refresh")

//    @PostMapping("/logout")

//    @GetMapping("/check-email/{email}")


    @PostMapping("/common-join")
    public ResponseEntity<ResponseDto<String>> commonJoin(@Valid @ModelAttribute JoinDto joinDto) {
        usersService.commonJoin(joinDto);
        return ResponseEntity.ok().body(ResponseDto.contentFrom("회원 가입이 완료되었습니다."));
    }

//    @PostMapping("/seller-join")

//    @PostMapping("/brewery-join")

//    @PostMapping("/reset-pw")

}
