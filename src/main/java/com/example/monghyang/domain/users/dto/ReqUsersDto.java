package com.example.monghyang.domain.users.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ReqUsersDto {
    private String email;
    private String curPassword; // 변경 전 현재 비밀번호
    private String newPassword; // 새 비밀번호
    private String nickname;
    private String name;
    private String phone;
    private LocalDate birth;
    private String gender;
    private String address;
    private String address_detail;
}
