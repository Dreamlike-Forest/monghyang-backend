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

    @Column(nullable = false, unique = true)
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
    @Column(nullable = false)
    private String addressDetail;
    @Column(unique = true)
    private String oAuth2Id;

    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isAgreed;
    private String refreshToken;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDate createdAt;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = false;

    @Builder(builderMethodName = "generalBuilder")
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

    @Builder(builderMethodName = "oAuth2Builder")
    public Users(String email, String name, String oAuth2Id, Role role) {
        this.email = email;
        this.name = name;
        this.oAuth2Id = oAuth2Id;
        this.role = role;
        this.password = "oAuth2User";
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAgreed(Boolean agreed) {
        isAgreed = agreed;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
