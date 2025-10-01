package com.example.monghyang.domain.global.pg.gateway;

import com.example.monghyang.domain.global.pg.component.AvailableAmount;
import com.example.monghyang.domain.global.pg.component.PendingAmount;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Balance { // 오픈마켓 쇼핑몰에서 셀러에게 지급할 수 있는 정산 잔액 정보
    private PendingAmount pendingAmount; // 셀러에게 지급할 수 없는 잔액 정보. 발생한 매출이 정산 주기 2일 전에 pendingAmount 금액으로 업데이트된다.
    private AvailableAmount availableAmount; // 셀러에게 지급할 수 있는 잔액 정보
}
