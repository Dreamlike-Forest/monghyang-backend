package com.example.monghyang.domain.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Role {
    @Id @GeneratedValue
    private Integer id;
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING) // Role 종류는 Enum으로 관리
    private RoleType name = RoleType.ROLE_USER; // 기본값: 일반 유저
}
