package com.example.monghyang.domain.global.pg.component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fee {
    private String type;                // 수수료 세부 타입(BASE, INSTALLMENT_DISCOUNT 등)
    private int fee;                    // 수수료 금액
}
