package com.example.monghyang.domain.orders.service;

import com.example.monghyang.domain.cart.entity.Cart;
import com.example.monghyang.domain.cart.repository.CartRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.dto.ReqOrderDto;
import com.example.monghyang.domain.orders.dto.ReqProductPreOrderDto;
import com.example.monghyang.domain.orders.repository.OrdersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;

    @Autowired
    public OrdersService(OrdersRepository ordersRepository, CartRepository cartRepository) {
        this.ordersRepository = ordersRepository;
        this.cartRepository = cartRepository;
    }

    // pre-order: uuid 반환
//    public UUID prepareOrder(Long userId, ReqProductPreOrderDto reqProductPreOrderDto) {
//        List<Cart> orderList = cartRepository.findByIdListAndUserId(reqProductPreOrderDto.getCart_id(), userId);
//        if(orderList.isEmpty()){
//            throw new ApplicationException(ApplicationError.CART_ITEM_NOT_FOUND);
//        }
//
//        // orders 생성
//
//        // 상품 주문 상태 내역 생성
//
//        // orderItem 생성
//
//        // 상품 요소 주문 상태 내역 생성
//
//        // 상품 환불 상태 내역 생성. 초기값: none
//
//        // uuid 반환
//    }

    // pg사로 결제 승인 요청

    // 자신의 모든 주문 조회

    // 자신의 주문 삭제

    // 자신의 주문 상태 내역 조회

    // 주문 상태 갱신
        // 해당 orders 레코드에 낙관적 락 -> orders 및 order_status_history 테이블에 쓰기 작업 -> 정합성 검증
    // '상태 변경자 유저 식별자'가 Null인 경우 시스템에서 상태를 갱신한 것
}
