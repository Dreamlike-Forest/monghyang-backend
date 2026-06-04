package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.global.annotation.validation.AllowNullNotBlankString;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "회원 공통 정보 수정 요청 정보")
public class ReqUsersDto {
    @Schema(description = "변경할 이메일입니다. null이면 변경하지 않습니다.", example = "user@example.com", nullable = true)
    @Email(message = "이메일 형식이어야 합니다.")
    private String email;
    @Schema(description = "비밀번호 변경 시 입력할 기존 비밀번호입니다.", example = "password1234!", nullable = true)
    @AllowNullNotBlankString
    private String curPassword; // 변경 전 현재 비밀번호
    @Schema(description = "비밀번호 변경 시 입력할 새 비밀번호입니다.", example = "newPassword1234!", nullable = true)
    @AllowNullNotBlankString
    private String newPassword; // 새 비밀번호
    @Schema(description = "변경할 닉네임 또는 상호명입니다. null이면 변경하지 않습니다.", example = "몽향회원", nullable = true)
    @AllowNullNotBlankString
    private String nickname;
    @Schema(description = "변경할 이름 또는 대표자명입니다. null이면 변경하지 않습니다.", example = "홍길동", nullable = true)
    @AllowNullNotBlankString
    private String name;
    @Schema(description = "변경할 연락처입니다. null이면 변경하지 않습니다.", example = "010-1234-5678", nullable = true)
    @AllowNullNotBlankString
    private String phone;
    @Schema(description = "변경할 생년월일입니다. yyyy-MM-dd 형식이며 null이면 변경하지 않습니다.", example = "1995-05-20", type = "string", format = "date", nullable = true)
    private LocalDate birth;
    @Schema(description = "변경할 성별입니다. man이면 남성, 그 외 값은 현재 구현상 여성으로 저장됩니다.", example = "man", allowableValues = {"man", "woman"}, nullable = true)
    @AllowNullNotBlankString
    private String gender;
    @Schema(description = "변경할 기본 주소입니다. null이면 변경하지 않습니다.", example = "서울시 중구 세종대로 110", nullable = true)
    @AllowNullNotBlankString
    private String address;
    @Schema(description = "변경할 상세 주소입니다. null이면 변경하지 않습니다.", example = "101호", nullable = true)
    @AllowNullNotBlankString
    private String address_detail;
}
