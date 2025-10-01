package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PendingAmount {
    private String currency; // 통화 (원화만 지원)
    private Integer value; // 잔액
}
