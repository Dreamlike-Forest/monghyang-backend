package com.example.monghyang.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JoinDto {
    @Email(message = "email 필드에 이메일 형식의 값을 입력해주세요.")
    @NotBlank(message = "email 값이 공백일 수 없습니다")
    private String email;
    @NotBlank(message = "password 값이 공백일 수 없습니다.")
    private String password;
    @NotBlank(message = "nickname 값이 공백일 수 없습니다.")
    private String nickname; // 일반회원: 닉네임, 양조장 및 판매자: 상호명
    @NotBlank(message = "name 값이 공백일 수 없습니다.")
    private String name; // 일반회원: 실명, 양조장 및 판매자: 대표자명
    @NotBlank(message = "phone 값이 공백일 수 없습니다.")
    private String phone;
    @NotNull(message = "birth 값이 공백일 수 없습니다.")
    private LocalDate birth;
    @NotNull(message = "gender 값이 공백일 수 없습니다.")
    private String gender; // false: 남자, true: 여자
    @NotBlank(message = "address 값이 공백일 수 없습니다.")
    private String address;
    @NotBlank(message = "address_detail 값이 공백일 수 없습니다.")
    private String address_detail;
    @NotNull(message = "is_agreed 값이 공백일 수 없습니다.")
    private Boolean is_agreed; // 약관 동의여부. 반드시 사용자로부터 약관동의를 받아야 하므로, 항상 true여야 하는 필드. 'true', 'false' 값을 받는다.
}
