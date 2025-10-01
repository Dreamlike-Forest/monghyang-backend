package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    // 은행 세 자리 코드
    private String bankCode;
    // 계좌번호
    private String accountNumber;
    // 예금주명
    private String holderName;
}
