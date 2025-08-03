package com.example.monghyang.domain.util.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtClaimsDto {
    private String tid;
    private Long userId;
    private String deviceType;
    private String role;

    private JwtClaimsDto(String tid, Long userId, String deviceType, String role) {
        this.tid = tid;
        this.userId = userId;
        this.deviceType = deviceType;
        this.role = role;
    }

    public static JwtClaimsDto tidUserIdDeviceTypeRoleOf(String tid, Long userId, String deviceType, String role) {
        return new JwtClaimsDto(tid, userId, deviceType, role);
    }
}
