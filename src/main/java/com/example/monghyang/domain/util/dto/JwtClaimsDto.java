package com.example.monghyang.domain.util.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtClaimsDto {
    private String tid;
    private Long userId;
    private String role;

    private JwtClaimsDto(String tid, Long userId, String role) {
        this.tid = tid;
        this.userId = userId;
        this.role = role;
    }

    public static JwtClaimsDto tidUserIdDeviceTypeRoleOf(String tid, Long userId, String role) {
        return new JwtClaimsDto(tid, userId, role);
    }
}
