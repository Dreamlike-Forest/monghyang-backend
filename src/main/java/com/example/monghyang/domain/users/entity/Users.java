package com.example.monghyang.domain.users.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {
    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "ROLE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Role role; // 기본값: 일반 유저(ROLE_USER)

    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String name;
    @Column(length = 20, nullable = false)
    private String phone;
    @Column(nullable = false)
    private LocalDate birth;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean gender;
    @Column(nullable = false)
    private String address;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isAgreed;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDate createdAt;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public Users(Role role, String email, String password, String nickname, String name, String phone, LocalDate birth, Boolean gender, String address, Boolean isAgreed) {
        this.role = role;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.phone = phone;
        this.birth = birth;
        this.gender = gender;
        this.address = address;
        this.isAgreed = isAgreed;
    }
}
