package com.example.monghyang.domain.orders.service;

import com.example.monghyang.domain.cart.entity.Cart;
import com.example.monghyang.domain.cart.repository.CartRepository;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.global.order.PaymentManager;
import com.example.monghyang.domain.global.order.ReqOrderDto;
import com.example.monghyang.domain.orders.dto.ReqPreOrderDto;
import com.example.monghyang.domain.orders.dto.ResOrderDto;
import com.example.monghyang.domain.orders.dto.ResOrderStatusHistoryDto;
import com.example.monghyang.domain.orders.entity.OrderStatusHistory;
import com.example.monghyang.domain.orders.entity.Orders;
import com.example.monghyang.domain.orders.item.dto.OrderItemDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemDto;
import com.example.monghyang.domain.orders.item.entity.*;
import com.example.monghyang.domain.orders.item.repository.OrderItemRepository;
import com.example.monghyang.domain.orders.item.service.OrderItemFulFillmentHistoryService;
import com.example.monghyang.domain.orders.item.service.OrderItemRefundHistoryService;
import com.example.monghyang.domain.orders.repository.OrderStatusHistoryRepository;
import com.example.monghyang.domain.orders.repository.OrdersRepository;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class OrdersService implements PaymentManager<ReqPreOrderDto> {
    @Getter
    private final int ORDER_PAGE_SIZE = 6;
    private final OrdersRepository ordersRepository;
    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final OrderItemFulFillmentHistoryService orderItemFulFillmentHistoryService;
    private final OrderItemRefundHistoryService orderItemRefundHistoryService;

    @Autowired
    public OrdersService(OrdersRepository ordersRepository, CartRepository cartRepository, UsersRepository usersRepository, OrderStatusHistoryRepository orderStatusHistoryRepository, OrderItemRepository orderItemRepository, ProductService productService, OrderStatusHistoryService orderStatusHistoryService, OrderItemFulFillmentHistoryService orderItemFulFillmentHistoryService, OrderItemRefundHistoryService orderItemRefundHistoryService) {
        this.ordersRepository = ordersRepository;
        this.cartRepository = cartRepository;
        this.usersRepository = usersRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.orderStatusHistoryService = orderStatusHistoryService;
        this.orderItemFulFillmentHistoryService = orderItemFulFillmentHistoryService;
        this.orderItemRefundHistoryService = orderItemRefundHistoryService;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UUID prepareOrder(Long userId, ReqPreOrderDto dto) {
        Users user = usersRepository.findById(userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.USER_NOT_FOUND));
        // 장바구니의 주문 대상 요소의 정보와, 해당 요소의 상품 정보까지 한번에 조회(fetch join)
        List<Cart> orderList = cartRepository.findByIdListAndUserId(dto.getCart_id(), userId);
        if(orderList.isEmpty()){
            throw new ApplicationException(ApplicationError.CART_ITEM_NOT_FOUND);
        } else if(orderList.size() != dto.getCart_id().size()) {
            // 주문 희망 장바구니 목록 중 품절/삭제 처리된 상품이 하나라도 있다면 주문 프로세스 중단
            throw new ApplicationException(ApplicationError.PRODUCT_NOT_FOUND);
        }

        // 상품 재고 차감: 하나라도 실패하면 주문 프로세스 종료
        productService.decreaseInventoryForOrder(orderList.stream().map(c -> c.getProduct().getId()).toList());

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

        // 상품 주문 상태 내역 생성: pending
        orderStatusHistoryService.setPaymentStatusPending(order);

        // orderItem 생성
        for(Cart cart : orderList){
            BigDecimal amount = cart.getProduct().getFinalPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .orders(order).product(cart.getProduct()).quantity(cart.getQuantity())
                    .amount(amount).provider(cart.getProduct().getUser()).build();
            // 각 주문 요소 엔티티, 해당 주문 요소의 배송 상태 및 환불 상태 엔티티 생성하고 Insert
            orderItemRepository.save(orderItem);
            orderItemFulFillmentHistoryService.setFulfillmentCreated(orderItem);
            orderItemRefundHistoryService.setRefundNone(orderItem);
            cartRepository.delete(cart);
        }

        // uuid 반환
        return pgOrderId;
    }

    @Override
    @Transactional
    public void requestOrderToPG(Long userId, ReqOrderDto dto) {
        Orders order = ordersRepository.findByPgOrderId(dto.getPg_order_id()).orElseThrow(() ->
                new ApplicationException(ApplicationError.ORDER_NOT_FOUND));
        if(!order.getUser().getId().equals(userId)){
            throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
        }
        if(!order.getTotalAmount().equals(dto.getTotal_amount())){
            throw new ApplicationException(ApplicationError.MANIPULATE_ORDER_TOTAL_PRICE);
        }

        /*

         PG사로 실제 결제를 요청하고 응답 결과를 처리하는 로직(외부 api 호출)
         => 별도의 메소드로 분리해서 컨트롤러에서 각각 따로따로 호출하도록 할 필요가 있어보임(DB 커넥션 과점유 방지)
         실패 시 history 테이블에도 저장

         */

        // 요청 결과에 따른 분기 처리

        // 주문 및 주문 상태 테이블 로직
        orderStatusHistoryService.updatePaymentStatusPaid(order);

        // 주문 요소 테이블 상태 갱신
        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(order.getId());
        if(orderItemList.isEmpty()){
            throw new ApplicationException(ApplicationError.ORDER_ITEM_NOT_FOUND);
        }
        for(OrderItem orderItem : orderItemList){
            orderItemFulFillmentHistoryService.updateFulfillmentAllocated(orderItem);
        }
    }

    @Override
    @Transactional
    public void setStatusFailed(UUID pgOrderId) {
        Orders orders = ordersRepository.findByPgOrderIdForSetFailed(pgOrderId).orElseThrow(() ->
                new ApplicationException(ApplicationError.ORDER_NOT_FOUND));
        orderStatusHistoryService.updatePaymentStatusFailed(orders);
    }

    public Page<ResOrderDto> getMyOrderList(Long userId, Integer startOffset) {
        if(startOffset == null || startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, ORDER_PAGE_SIZE);
        Page<ResOrderDto> result = ordersRepository.findByUserId(pageable, userId);
        if(result.getContent().isEmpty()) {
            throw new ApplicationException(ApplicationError.ORDER_NOT_FOUND);
        }
        List<OrderItemDto> orderItemList = orderItemRepository.findByOrderIdList(
                result.getContent().stream().map(ResOrderDto::getOrder_id).toList()
        );

        for(OrderItemDto orderItemDto : orderItemList){
            ResOrderItemDto resOrderItemDto = new ResOrderItemDto(orderItemDto);

            for(ResOrderDto resOrderDto : result.getContent()){
                if(resOrderDto.getOrder_id().equals(resOrderItemDto.getOrder_id())){
                    resOrderDto.getOrder_items().add(resOrderItemDto);
                }
            }
        }
        return result;
    }

    // 자신의 특정 주문의 상태 변경 내역 조회
    public List<ResOrderStatusHistoryDto> getMyOrderStatusHistory(Long userId, Long orderId) {
        Orders orders = ordersRepository.findById(orderId).orElseThrow(() ->
                new ApplicationException(ApplicationError.ORDER_NOT_FOUND));
        if(!orders.getUser().getId().equals(userId)){
            throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
        }

        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderId(orderId);
        if(history.isEmpty()){
            throw new ApplicationException(ApplicationError.HISTORY_NOT_FOUND);
        }

        return history.stream().map(ResOrderStatusHistoryDto::new).toList();
    }
}
