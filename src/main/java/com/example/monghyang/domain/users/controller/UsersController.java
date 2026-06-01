package com.example.monghyang.domain.users.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.annotation.auth.LoginUserRole;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.users.dto.ReqUsersDto;
import com.example.monghyang.domain.users.dto.ResUsersDto;
import com.example.monghyang.domain.users.dto.ResUsersPrivateInfoDto;
import com.example.monghyang.domain.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "회원 공통 API")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;
    private final RedisService redisService;
    private final SecurityContextLogoutHandler securityContextLogoutHandler; // 스프링 세션 표준 로그아웃 핸들러

    @GetMapping("/email/{email}")
    @Operation(summary = "Email로 회원을 조회합니다.")
    public ResponseEntity<ResponseDataDto<List<ResUsersDto>>> getUsersByEmail(@PathVariable String email) {
        List<ResUsersDto> resUsersDto = usersService.getUsersByEmail(email);
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(resUsersDto));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "유저 식별자로 조회")
    public ResponseEntity<ResponseDataDto<ResUsersDto>> getUsersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(usersService.getUsersById(userId)));
    }

    @GetMapping("/my")
    @Operation(
            summary = "내 정보 조회",
            description = "현재 로그인한 회원의 기본 정보와 역할별 추가 정보를 조회합니다. 양조장 회원은 brewery 필드, 판매자 회원은 seller 필드가 포함되며, 해당하지 않는 필드는 응답에서 제외됩니다.",
            security = @SecurityRequirement(name = "SessionID")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "content": {
                                        "users_id": 10,
                                        "role_name": "ROLE_BREWERY",
                                        "users_email": "brewery@example.com",
                                        "users_nickname": "몽향양조장",
                                        "users_name": "홍길동",
                                        "users_phone": "010-1234-5678",
                                        "users_birth": "1995-05-20",
                                        "users_gender": "man",
                                        "users_address": "서울시 중구",
                                        "users_address_detail": "101호",
                                        "brewery": {
                                          "brewery_id": 1,
                                          "region_type_id": 1,
                                          "region_type_name": "서울",
                                          "brewery_name": "몽향양조장",
                                          "brewery_images": []
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원, 양조장 또는 판매자 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<ResUsersPrivateInfoDto>> getMyUserInfo(
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(usersService.getMyUserInfo(userId)));
    }

    // 회원 정보 수정 api
    @PostMapping("/update")
    @Operation(summary = "회원 수정 api", description = "비밀번호 변경 시 '기존 비밀번호'와 '새 비밀번호'를 각각의 필드에 입력하여 전송해주셔야 합니다. 수정 성공 시 해당 유저의 모든 로그인 상태 정보가 서버에서 제거됩니다.")
    public ResponseEntity<ResponseDataDto<Void>> updateUsers(
            @LoginUserId Long userId, @LoginUserRole String userRole,
            @Valid @ModelAttribute ReqUsersDto reqUsersDto,
            HttpServletRequest request, HttpServletResponse response) {
        usersService.updateUsers(userId, reqUsersDto, userRole);

        // 해당 유저의 현재 세션 정보를 제거
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null) {
            securityContextLogoutHandler.logout(request, response, auth);
        }
        // 해당 유저의 나머지 모든 세션 정보 제거
        redisService.deleteAllInfoByUserId(userId);

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
        redisService.deleteAllInfoByUserId(userId);
        return ResponseEntity.ok().body(ResponseDataDto.success("회원 탈퇴가 완료되었습니다."));
    }
}
