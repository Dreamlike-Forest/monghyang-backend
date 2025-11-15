package com.example.monghyang.domain.users.dto;

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
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String curPassword; // 변경 전 현재 비밀번호
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String newPassword; // 새 비밀번호
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String nickname;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String name;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String phone;
    private LocalDate birth;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String gender;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String address;
    @Pattern(regexp = "^[^\\s].*\\S.*$", message = "빈 문자열이나 공백만 입력할 수 없습니다")
    private String address_detail;
}
