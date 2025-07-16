package com.example.monghyang.domain.users.controller;

import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.global.response.ResponseDto;
import com.example.monghyang.domain.users.dto.JoinDto;
import com.example.monghyang.domain.users.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto> tokenRefresh(HttpServletRequest request, HttpServletResponse response) {
        usersService.updateRefreshToken(request, response);
        return ResponseEntity.ok().body(new ResponseDto());
    }

//    @PostMapping("/logout")

//    @GetMapping("/check-email/{email}")


    @PostMapping("/common-join")
    public ResponseEntity<ResponseDto> commonJoin(@Valid @ModelAttribute JoinDto joinDto) {
        usersService.commonJoin(joinDto);
        return ResponseEntity.ok().body(new ResponseDto());
    }

//    @PostMapping("/seller-join")

//    @PostMapping("/brewery-join")

//    @PostMapping("/reset-pw")

}
