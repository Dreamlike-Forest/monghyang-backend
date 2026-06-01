package com.example.monghyang.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "회원가입 공통 요청 정보")
public class JoinDto {
    @Schema(description = "로그인에 사용할 이메일입니다.", example = "brewery@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "email 필드에 이메일 형식의 값을 입력해주세요.")
    @NotBlank(message = "email 값이 공백일 수 없습니다")
    private String email;
    @Schema(description = "로그인에 사용할 비밀번호입니다.", example = "password1234!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "password 값이 공백일 수 없습니다.")
    private String password;
    @Schema(description = "일반 회원은 닉네임, 양조장 및 판매자는 상호명으로 사용하는 값입니다.", example = "몽향양조장", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "nickname 값이 공백일 수 없습니다.")
    private String nickname; // 일반회원: 닉네임, 양조장 및 판매자: 상호명
    @Schema(description = "일반 회원은 실명, 양조장 및 판매자는 대표자명으로 사용하는 값입니다.", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "name 값이 공백일 수 없습니다.")
    private String name; // 일반회원: 실명, 양조장 및 판매자: 대표자명
    @Schema(description = "회원 연락처입니다.", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "phone 값이 공백일 수 없습니다.")
    private String phone;
    @Schema(description = "회원 생년월일입니다. yyyy-MM-dd 형식으로 전달합니다.", example = "1995-05-20", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "birth 값이 공백일 수 없습니다.")
    private LocalDate birth;
    @Schema(description = "회원 성별입니다. man이면 남성, 그 외 값은 현재 구현상 여성으로 저장됩니다.", example = "man", allowableValues = {"man", "woman"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "gender 값이 공백일 수 없습니다.")
    private String gender; // man: 남성, 그 외 값: 여성
    @Schema(description = "회원 기본 주소입니다.", example = "서울시 중구 세종대로 110", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "address 값이 공백일 수 없습니다.")
    private String address;
    @Schema(description = "회원 상세 주소입니다.", example = "101호", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "address_detail 값이 공백일 수 없습니다.")
    private String address_detail;
    @Schema(description = "플랫폼 이용 약관 동의 여부입니다. 회원가입하려면 true여야 합니다.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "is_agreed 값이 공백일 수 없습니다.")
    private Boolean is_agreed; // 약관 동의여부. 반드시 사용자로부터 약관동의를 받아야 하므로, 항상 true여야 하는 필드. 'true', 'false' 값을 받는다.
}
