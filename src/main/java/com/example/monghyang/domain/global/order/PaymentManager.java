package com.example.monghyang.domain.global.order;

import java.util.UUID;

public interface PaymentManager<T> {
    /**
     * 프론트에서 PG 요청에 필요한 orderId 발급 및 총 결제 금액 계산, 주문 요소 DB 저장
     * @param userId 유저 식별자(@LoginUserId로 얻은 userId)
     * @param dto 주문 대상 장바구니 식별자 및 주문 관련 정보가 담긴 dto
     * @return UUID 형식의 orderId
     */
    UUID prepareOrder(Long userId, T dto);

    /**
     * 실제 PG사로 결제 승인 요청, 성공 및 실패에 따른 처리
     * @param userId 유저 식별자(@LoginUserId로 얻은 userId)
     * @param dto paymentKey, orderId, total_amount 값이 담긴 dto
     */
    void requestOrderToPG(Long userId, ReqOrderDto dto);
}
