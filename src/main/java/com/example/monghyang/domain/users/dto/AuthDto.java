package com.example.monghyang.domain.users.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthDto {
    private Long userId;
    private String email;
    private String roleType;
    private String password;
}
