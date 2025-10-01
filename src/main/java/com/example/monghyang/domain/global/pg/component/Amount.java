package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Amount {
    // 통화 (예: KRW)
    private String currency;
    // 지급 금액
    private Double value;
}
