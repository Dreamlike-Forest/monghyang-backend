package com.example.monghyang.domain.orders.service;

import com.example.monghyang.domain.orders.repository.OrdersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrdersService {
    private final OrdersRepository ordersRepository;
    @Autowired
    public OrdersService(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    // pre-order: uuid 반환

    // pg사로 결제 승인 요청

    // 자신의 모든 주문 조회

    // 자신의 주문 삭제

    // 자신의 주문 상태 내역 조회

    // 주문 상태 갱신
        // 해당 orders 레코드에 낙관적 락 -> orders 및 order_status_history 테이블에 쓰기 작업 -> 정합성 검증
    // '상태 변경자 유저 식별자'가 Null인 경우 시스템에서 상태를 갱신한 것
}
