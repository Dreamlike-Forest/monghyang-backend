package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.users.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Schema(description = "회원 공개 조회 응답 정보")
public class ResUsersDto {
    @Schema(description = "회원 식별자입니다.", example = "10")
    private final Long users_id;
    @Schema(description = "회원 역할명입니다.", example = "ROLE_USER")
    private final String role_name;
    @Schema(description = "회원 이메일입니다.", example = "user@example.com")
    private final String users_email;
    @Schema(description = "회원 닉네임 또는 상호명입니다.", example = "몽향회원")
    private final String users_nickname;
    @Schema(description = "회원 이름 또는 대표자명입니다.", example = "홍길동")
    private final String users_name;
    @Schema(description = "회원 성별입니다.", example = "man")
    private final String users_gender;

    private ResUsersDto(Users users) {
        this.users_id = users.getId();
        this.role_name = users.getRole().getName().getRoleName();
        this.users_email = users.getEmail();
        this.users_nickname = users.getNickname();
        this.users_name = users.getName();
        this.users_gender = (users.getGender() == Boolean.FALSE) ? "man" : "woman";
    }

    public static ResUsersDto usersJoinedWithRoleToDto(Users users) {
        return new ResUsersDto(users);
    }
}
