package com.example.monghyang.domain.orders.item.service;

import com.example.monghyang.domain.orders.item.entity.OrderItem;
import com.example.monghyang.domain.orders.item.entity.OrderItemRefundHistory;
import com.example.monghyang.domain.orders.item.entity.RefundStatus;
import com.example.monghyang.domain.orders.item.repository.OrderItemRefundHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderItemRefundHistoryService {
    private final OrderItemRefundHistoryRepository orderItemRefundHistoryRepository;

    @Transactional
    public void setRefundNone(OrderItem orderItem) {
        orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.NONE));
    }

    // '환불 요청' 상태 설정
    @Transactional
    public void updateRefundRequested(OrderItem orderItem) {
        orderItem.updateRefundStatus(RefundStatus.REQUESTED);
        orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.REQUESTED));
    }
    // '반품 되었음' 상태 설정
    @Transactional
    public void updateRefundReturned(OrderItem orderItem) {
        orderItem.updateRefundStatus(RefundStatus.REQUESTED);
        orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.RETURNED));
    }
    // '환불 처리됨' 상태 설정 및 환불 처리 로직
    @Transactional
    public void updateRefundRefunded(OrderItem orderItem) {
        /*
            PG사를 통해 해당 주문 요소 결제 금액에 해당하는 금액을 환불
         */
        orderItem.updateRefundStatus(RefundStatus.REFUNDED); // 환불 요청 상태
        orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.REFUNDED));
        // 동일 Orders의 다른 요소를 조회해서 모두 환불 상태면 Orders의 '결제 상태'를 환불 상태로 변경
        // 일부만 환불 상태라면 Orders의 '결제 상태'를 일부 환불 상태로 변경
    }
}
