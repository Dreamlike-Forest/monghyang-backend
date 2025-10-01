package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Company {
    // 사업자명
    private String name;
    // 대표자명
    private String representativeName;
    // 사업자번호
    private String businessRegistrationNumber;
    // 사업자 이메일 (KYC 심사 안내가 전송됨)
    private String email;
    // 사업자 전화번호
    private String phone;
}
