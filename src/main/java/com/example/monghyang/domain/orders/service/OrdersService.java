package com.example.monghyang.domain.orders.service;

import com.example.monghyang.domain.cart.entity.Cart;
import com.example.monghyang.domain.cart.repository.CartRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.global.order.PaymentManager;
import com.example.monghyang.domain.global.order.ReqOrderDto;
import com.example.monghyang.domain.orders.dto.ReqProductPreOrderDto;
import com.example.monghyang.domain.orders.entity.OrderStatusHistory;
import com.example.monghyang.domain.orders.entity.Orders;
import com.example.monghyang.domain.orders.entity.PaymentStatus;
import com.example.monghyang.domain.orders.item.entity.*;
import com.example.monghyang.domain.orders.item.repository.OrderItemFulfillmentHistoryRepository;
import com.example.monghyang.domain.orders.item.repository.OrderItemRefundHistoryRepository;
import com.example.monghyang.domain.orders.item.repository.OrderItemRepository;
import com.example.monghyang.domain.orders.repository.OrderStatusHistoryRepository;
import com.example.monghyang.domain.orders.repository.OrdersRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OrdersService implements PaymentManager<ReqProductPreOrderDto> {
    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemFulfillmentHistoryRepository orderItemFulfillmentHistoryRepository;
    private final OrderItemRefundHistoryRepository orderItemRefundHistoryRepository;

    @Autowired
    public OrdersService(OrdersRepository ordersRepository, CartRepository cartRepository, UsersRepository usersRepository, OrderStatusHistoryRepository orderStatusHistoryRepository, OrderItemRepository orderItemRepository, OrderItemFulfillmentHistoryRepository orderItemFulfillmentHistoryRepository, OrderItemRefundHistoryRepository orderItemRefundHistoryRepository) {
        this.ordersRepository = ordersRepository;
        this.cartRepository = cartRepository;
        this.usersRepository = usersRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemFulfillmentHistoryRepository = orderItemFulfillmentHistoryRepository;
        this.orderItemRefundHistoryRepository = orderItemRefundHistoryRepository;
    }

    @Override
    @Transactional
    public UUID prepareOrder(Long userId, ReqProductPreOrderDto dto) {
        // 장바구니의 주문 대상 요소의 정보와, 해당 요소의 상품 정보까지 한번에 조회(fetch join)
        List<Cart> orderList = cartRepository.findByIdListAndUserId(dto.getCart_id(), userId);
        if(orderList.isEmpty()){
            throw new ApplicationException(ApplicationError.CART_ITEM_NOT_FOUND);
        }
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        // uuid 생성
        UUID pgOrderId = UUID.randomUUID();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for(Cart cart : orderList){
            // 장바구니의 주문 요청 대상과 수량 정보를 통해 총 결제 금액 계산
            BigDecimal quantity = BigDecimal.valueOf(cart.getQuantity());
            BigDecimal cur = cart.getProduct().getFinalPrice().multiply(quantity);
            totalAmount = totalAmount.add(cur);
        }

        // orders 생성
        Orders order = Orders.builder()
                .user(user).totalAmount(totalAmount).pgOrderId(pgOrderId).payerName(dto.getPayer_name())
                .payerPhone(dto.getPayer_phone()).address(dto.getAddress())
                .addressDetail(dto.getAddress_detail()).build();
        ordersRepository.save(order);

        // 상품 주문 상태 내역 생성
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.orderToStatusReasonCodeOf(order, PaymentStatus.PENDING, "pending");
        orderStatusHistoryRepository.save(orderStatusHistory);

        // orderItem 생성
        for(Cart cart : orderList){
            BigDecimal amount = cart.getProduct().getFinalPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .orders(order).product(cart.getProduct()).quantity(cart.getQuantity()).amount(amount).build();
            // 각 주문 요소 엔티티, 해당 주문 요소의 배송 상태 및 환불 상태 엔티티 생성하고 Insert
            orderItemRepository.save(orderItem);
            orderItemFulfillmentHistoryRepository.save(
                    OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.CREATED, "created"));
            orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.NONE));
        }

        // uuid 반환
        return pgOrderId;
    }

    @Override
    @Transactional
    public void requestOrderToPG(Long userId, ReqOrderDto dto) {
        // PG사로 실제 결제 승인 요청

        // 요청 결과에 따른 분기 처리

    }

    // 자신의 모든 주문 조회

    // 자신의 주문 삭제

    // 자신의 주문 상태 내역 조회

    // 주문 상태 갱신
        // 해당 orders 레코드에 낙관적 락 -> orders 및 order_status_history 테이블에 쓰기 작업 -> 정합성 검증
    // '상태 변경자 유저 식별자'가 Null인 경우 시스템에서 상태를 갱신한 것
}
