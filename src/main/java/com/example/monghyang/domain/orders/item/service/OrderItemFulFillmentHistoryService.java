package com.example.monghyang.domain.orders.item.service;

import com.example.monghyang.domain.orders.item.entity.FulfillmentStatus;
import com.example.monghyang.domain.orders.item.entity.OrderItem;
import com.example.monghyang.domain.orders.item.entity.OrderItemFulfillmentHistory;
import com.example.monghyang.domain.orders.item.repository.OrderItemFulfillmentHistoryRepository;
import com.example.monghyang.domain.orders.item.repository.OrderItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderItemFulFillmentHistoryService {
    private final OrderItemFulfillmentHistoryRepository orderItemFulfillmentHistoryRepository;

    @Autowired
    public OrderItemFulFillmentHistoryService(OrderItemFulfillmentHistoryRepository orderItemFulfillmentHistoryRepository) {
        this.orderItemFulfillmentHistoryRepository = orderItemFulfillmentHistoryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setFulfillmentCreated(OrderItem orderItem) {
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.CREATED, "created"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFulfillmentAllocated(OrderItem orderItem) {
        orderItem.updateFulfillmentStatus(FulfillmentStatus.ALLOCATED);
        orderItemFulfillmentHistoryRepository.save(OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.ALLOCATED, "allocated"));
    }

    // '배송 준비' 상태 설정
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFulfillmentPacked(OrderItem orderItem) {
        orderItem.updateFulfillmentStatus(FulfillmentStatus.PACKED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.PACKED, "packed"));
    }
    // '배송 중' 상태 설정
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFulfillmentInTransit(OrderItem orderItem) {
        orderItem.updateFulfillmentStatus(FulfillmentStatus.IN_TRANSIT);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.IN_TRANSIT, "in-transit"));
    }
    // '배송 완료' 상태 설정
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFulfillmentDelivered(OrderItem orderItem) {
        orderItem.updateFulfillmentStatus(FulfillmentStatus.DELIVERED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.DELIVERED, "delivered"));
    }

    /**
     * 취소 처리
     * @param orderItem 취소할 주문 요소
     * @param isProvider 취소를 수행하는 주체가 상품 게시자인지 나타내는 플래그
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFulfillmentCanceled(OrderItem orderItem, boolean isProvider) {
        String message = (isProvider) ? "canceled by provider" : "canceled by user";
        orderItem.updateFulfillmentStatus(FulfillmentStatus.CANCELED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.CANCELED, message));
    }


    // '배송 실패' 상태 설정
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFulfillmentFailed(OrderItem orderItem) {
        orderItem.updateFulfillmentStatus(FulfillmentStatus.FAILED);
        orderItemFulfillmentHistoryRepository.save(
                OrderItemFulfillmentHistory.orderItemToStatusReasonCodeOf(orderItem, FulfillmentStatus.FAILED, "failed"));
    }


}
