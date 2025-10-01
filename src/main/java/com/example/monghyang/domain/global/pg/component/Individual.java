package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Individual {
    // 개인 이름
    private String name;
    // 개인 이메일 (KYC 심사 안내가 전송됨)
    private String email;
    // 개인 전화번호
    private String phone;
}
