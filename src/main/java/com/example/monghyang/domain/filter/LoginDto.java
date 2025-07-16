package com.example.monghyang.domain.filter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginDto {
    private String nickname;
    private String role;

    private LoginDto(String nickname, String role) {
        this.nickname = nickname;
        this.role = role;
    }

    public static LoginDto nicknameRoleOf(String nickname, String role) {
        return new LoginDto(nickname, role);
    }
}
