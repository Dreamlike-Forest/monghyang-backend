package com.example.monghyang.domain.users.entity;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(length = 20, nullable = false, unique = true)
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
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDate createdAt;
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Builder(builderMethodName = "generalBuilder") // 일반 회원 플랫폼 회원가입
    public Users(@NonNull Role role, @NonNull String email, @NonNull String password, @NonNull String nickname, @NonNull String name, @NonNull String phone, @NonNull LocalDate birth, @NonNull Boolean gender, @NonNull String address, @NonNull String address_detail, @NonNull Boolean isAgreed) {
        if(isAgreed == Boolean.FALSE) {
            // 약관에 동의하지 않으면 회원가입 불가
            throw new ApplicationException(ApplicationError.TERMS_AND_CONDITIONS_NOT_AGREED);
        }
        this.role = role;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.phone = phone;
        this.birth = birth;
        this.gender = gender;
        this.address = address;
        this.addressDetail = address_detail;
        this.isAgreed = isAgreed;
    }

    @Builder(builderMethodName = "oAuth2Builder") // OAuth2 회원가입 -> 회원가입에 필요한 추가 정보 입력 페이지로 리다이렉션 필요
    public Users(String email, String name, String oAuth2Id, Role role) {
        this.email = email;
        this.name = name;
        this.oAuth2Id = oAuth2Id;
        this.role = role; // OAuth2 회원가입은 '일반 사용자'만 가능합니다.
        this.password = "oAuth2User";
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public void updateBirth(LocalDate birth) {
        this.birth = birth;
    }

    public void updateGender(String gender) {
        if(gender.equals("man")) {
            this.gender = Boolean.FALSE;
        } else {
            this.gender = Boolean.TRUE;
        }
    }

    public void updateRole(Role role) {
        // 권한 변경
        this.role = role;
    }

    public void setDeleted() {
        // 유저 삭제 처리
        this.isDeleted = Boolean.TRUE;
    }
    public void unSetDeleted() {
        // 유저 삭제 처리 복구(휴면 회원 복구 등에 사용)
        this.isDeleted = Boolean.FALSE;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }
}
