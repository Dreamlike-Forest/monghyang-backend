package com.example.monghyang.domain.users.dto;

import com.example.monghyang.domain.users.entity.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 간단 정보")
public record UserSimpleInfoDto(
        @Schema(description = "회원 닉네임 또는 상호명입니다.", example = "몽향회원")
        String nickname,
        @Schema(description = "회원 역할 타입입니다.", example = "ROLE_USER")
        RoleType roleType
) { }
