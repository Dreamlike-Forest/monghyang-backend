package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ReqUsersDto {
    @Email(message = "이메일 형식이어야 합니다.")
    private String email;
    @AllowNullNotBlankString
    private String curPassword; // 변경 전 현재 비밀번호
    @AllowNullNotBlankString
    private String newPassword; // 새 비밀번호
    @AllowNullNotBlankString
    private String nickname;
    @AllowNullNotBlankString
    private String name;
    @AllowNullNotBlankString
    private String phone;
    private LocalDate birth;
    @AllowNullNotBlankString
    private String gender;
    @AllowNullNotBlankString
    private String address;
    @AllowNullNotBlankString
    private String address_detail;
}
