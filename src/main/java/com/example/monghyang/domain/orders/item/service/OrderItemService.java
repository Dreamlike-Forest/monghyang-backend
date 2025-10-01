package com.example.monghyang.domain.orders.item.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderListDto;
import com.example.monghyang.domain.orders.item.entity.*;
import com.example.monghyang.domain.orders.item.repository.OrderItemFulfillmentHistoryRepository;
import com.example.monghyang.domain.orders.item.repository.OrderItemRefundHistoryRepository;
import com.example.monghyang.domain.orders.item.repository.OrderItemRepository;
import com.example.monghyang.domain.orders.repository.OrdersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderItemService {
    private final int ORDER_PAGE_SIZE = 6;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemFulfillmentHistoryRepository orderItemFulfillmentHistoryRepository;
    private final OrderItemRefundHistoryRepository orderItemRefundHistoryRepository;
    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository, OrderItemFulfillmentHistoryRepository orderItemFulfillmentHistoryRepository, OrderItemRefundHistoryRepository orderItemRefundHistoryRepository, OrdersRepository ordersRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderItemFulfillmentHistoryRepository = orderItemFulfillmentHistoryRepository;
        this.orderItemRefundHistoryRepository = orderItemRefundHistoryRepository;
        this.ordersRepository = ordersRepository;
    }

    // 주문 요소를 조회하고 소유권 검증
    private OrderItem verifyOrderItemIsOwn(Long providerId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(() ->
                new ApplicationException(ApplicationError.ORDER_ITEM_NOT_FOUND));
        if(!orderItem.getProvider().getId().equals(providerId)){
            throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
        }
        return orderItem;
    }
    // '배송 준비' 상태 설정
    @Transactional
    public void updateFulfillmentPacked(Long providerId, Long orderItemId) {
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateFulfillmentStatus(FulfillmentStatus.PACKED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.PACKED, "packed"));
    }
    // '배송 중' 상태 설정
    @Transactional
    public void updateFulfillmentInTransit(Long providerId, Long orderItemId) {
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateFulfillmentStatus(FulfillmentStatus.IN_TRANSIT);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.IN_TRANSIT, "in-transit"));
    }
    // '배송 완료' 상태 설정
    @Transactional
    public void updateFulfillmentDelivered(Long providerId, Long orderItemId) {
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateFulfillmentStatus(FulfillmentStatus.DELIVERED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.DELIVERED, "delivered"));
    }
    // '배송 취소' 상태 설정
    @Transactional
    public void updateFulfillmentCanceled(Long providerId, Long orderItemId) {
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateFulfillmentStatus(FulfillmentStatus.CANCELED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.CANCELED, "canceled"));
    }
    // '배송 실패' 상태 설정
    @Transactional
    public void updateFulfillmentFailed(Long providerId, Long orderItemId) {
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateFulfillmentStatus(FulfillmentStatus.FAILED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.FAILED, "failed"));
    }
    // '환불 요청' 상태 설정
    @Transactional
    public void updateRefundRequested(Long providerId, Long orderItemId) {
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateRefundStatus(RefundStatus.REQUESTED);
        orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.REQUESTED));
    }
    // '반품 완료' 상태 설정
    @Transactional
    public void updateRefundReturned(Long providerId, Long orderItemId) {
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateRefundStatus(RefundStatus.REQUESTED);
        orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.RETURNED));
    }
    // '환불 처리됨' 상태 설정 및 환불 처리 로직
    @Transactional
    public void updateRefundRefunded(Long providerId, Long orderItemId) {
        /*
            PG사를 통해 해당 주문 요소 결제 금액에 해당하는 금액을 환불
         */
        OrderItem orderItem = verifyOrderItemIsOwn(providerId, orderItemId);
        orderItem.updateRefundStatus(RefundStatus.REFUNDED); // 환불 요청 상태
        orderItemRefundHistoryRepository.save(OrderItemRefundHistory.orderItemToStatusOf(orderItem, RefundStatus.REFUNDED));
        // 동일 Orders의 다른 요소를 조회해서 모두 환불 상태면 Orders의 '결제 상태'를 환불 상태로 변경
        // 일부만 환불 상태라면 Orders의 '결제 상태'를 일부 환불 상태로 변경

    }

    // 특정 유저의 주문 요소 리스트 조회
    // 주문 6개에 해당하는 주문 요소 반환
    public Page<ResOrderListDto> getMyOrderItemListPaging(Long userId, Integer startOffset) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(startOffset, ORDER_PAGE_SIZE, sort);
        // 1. 특정 유저의 Order의 최근 6개 데이터 조회(pending 상태 제외): orderId, createdAt 정보 조회
        Page<ResOrderListDto> orderInfoList = ordersRepository.findIdByUserIdPaging(userId, pageable);
        // Order 정보와 Item 정보를 매핑하기 위한 HashMap
        Map<Long, ResOrderListDto> map = new HashMap<>();
        for(ResOrderListDto resOrderListDto : orderInfoList.getContent()) {
            map.put(resOrderListDto.getOrder_id(), resOrderListDto);
        }

        // 2. 1번에서 얻은 6개의 orderId로 OrderItem 조회
        List<OrderItem> orderItemList = orderItemRepository.findByOrderIdList(orderInfoList.stream().map(ResOrderListDto::getOrder_id).toList());
        if(orderItemList.isEmpty()) {
            return orderInfoList;
        }

        // 3. orderItem의 orderId 필드에 매치되는 ResOrderListDto의 ItemDto 리스트 필드에 orderItem을 Dto로 변환한 인스턴스를 추가
        for(OrderItem orderItem : orderItemList) {
            ResOrderListDto info = map.get(orderItem.getOrders().getId());
            if(info == null) continue; // 식별자가 Map에 존재하지 않는 경우 건너뛴다.
            info.getOrder_item_list().add(new ResOrderItemDto(orderItem));
        }

        return orderInfoList;
    }

}
