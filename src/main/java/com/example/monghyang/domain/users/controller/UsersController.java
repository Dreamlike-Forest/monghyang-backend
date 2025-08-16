package com.example.monghyang.domain.users.controller;

import com.example.monghyang.domain.global.annotation.LoginUserId;
import com.example.monghyang.domain.global.annotation.LoginUserRole;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.users.dto.ReqUsersDto;
import com.example.monghyang.domain.users.dto.ResUsersDto;
import com.example.monghyang.domain.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "회원 공통 API")
public class UsersController {
    private final UsersService usersService;
    private final RedisService redisService;
    private final SecurityContextLogoutHandler securityContextLogoutHandler; // 스프링 세션 표준 로그아웃 핸들러

    @Autowired
    public UsersController(UsersService usersService, RedisService redisService, SecurityContextLogoutHandler securityContextLogoutHandler) {
        this.usersService = usersService;
        this.redisService = redisService;
        this.securityContextLogoutHandler = securityContextLogoutHandler;
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Email로 회원을 조회합니다.")
    public ResponseEntity<ResponseDataDto<ResUsersDto>> getUsersByEmail(@PathVariable String email) {
        ResUsersDto resUsersDto = usersService.getUsersByEmail(email);
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(resUsersDto));
    }

    // 회원 정보 수정 api
    @PostMapping("/update")
    @Operation(summary = "회원 수정 api", description = "비밀번호 변경 시 '기존 비밀번호'와 '새 비밀번호'를 각각의 필드에 입력하여 전송해주셔야 합니다. 수정 성공 시 해당 유저의 모든 로그인 상태 정보가 서버에서 제거됩니다.")
    public ResponseEntity<ResponseDataDto<Void>> updateUsers(
            @LoginUserId Long userId, @LoginUserRole String userRole,
            @ModelAttribute ReqUsersDto reqUsersDto,
            HttpServletRequest request, HttpServletResponse response) {
        System.out.println("유저 식별자: "+userId+", 유저 권한: "+userRole);

        usersService.updateUsers(userId, reqUsersDto, userRole);

        // 해당 유저의 현재 세션 정보를 제거
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null) {
            securityContextLogoutHandler.logout(request, response, auth);
        }
        // 해당 유저의 나머지 모든 세션 정보 및 refresh token 정보를 제거
        redisService.deleteAllInfo(userId);

        return ResponseEntity.ok().body(ResponseDataDto.success("회원 수정이 완료되었습니다. 다시 로그인 해주세요."));
    }


    // 회원 탈퇴 api
    @DeleteMapping
    @Operation(summary = "회원 탈퇴 API")
    public ResponseEntity<ResponseDataDto<Void>> deleteUsers(
            @LoginUserId Long userId, @LoginUserRole String userRole,
            HttpServletRequest request,
            HttpServletResponse response) {

        usersService.withdrawalUser(userId, userRole); // 회원 탈퇴 로직(soft delete)
        // 해당 유저의 현재 세션 정보를 제거
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null) {
            securityContextLogoutHandler.logout(request, response, auth);
        }
        // 해당 유저의 나머지 모든 세션 정보 및 refresh token 정보를 제거
        redisService.deleteAllInfo(userId);
        return ResponseEntity.ok().body(ResponseDataDto.success("회원 탈퇴가 완료되었습니다."));
    }
}
