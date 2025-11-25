package com.example.monghyang.domain.orders.item.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.orders.item.dto.ResFulfillmentStatusHistoryDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemForSellerDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemStatusHistoryDto;
import com.example.monghyang.domain.orders.item.dto.ResRefundStatusHistoryDto;
import com.example.monghyang.domain.orders.item.entity.*;
import com.example.monghyang.domain.orders.item.repository.OrderItemFulfillmentHistoryRepository;
import com.example.monghyang.domain.orders.item.repository.OrderItemRefundHistoryRepository;
import com.example.monghyang.domain.orders.item.repository.OrderItemRepository;
import com.example.monghyang.domain.orders.service.OrderStatusHistoryService;
import com.example.monghyang.domain.product.service.ProductService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderItemService {
    @Getter
    private final int ORDER_ITEM_PAGE_SIZE = 12;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final OrderItemFulFillmentHistoryService orderItemFulFillmentHistoryService;
    private final OrderItemRefundHistoryService orderItemRefundHistoryService;
    private final OrderStatusHistoryService orderStatusHistoryService;
    private final OrderItemFulfillmentHistoryRepository orderItemFulfillmentHistoryRepository;
    private final OrderItemRefundHistoryRepository orderItemRefundHistoryRepository;

    /**
     * 개별 주문 요소 1개 취소
     * @param userId 주문자 식별자
     * @param orderItemId 취소할 주문 요소 식별자
     */
    @Transactional
    public void cancelOrderItem(Long userId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findByPaidOrderItemIdAndUserId(orderItemId, userId).orElseThrow(() ->
                new ApplicationException(ApplicationError.ORDER_ITEM_NOT_FOUND));
        FulfillmentStatus curStatus = orderItem.getFulfillmentStatus();
        if(curStatus.equals(FulfillmentStatus.IN_TRANSIT) || curStatus.equals(FulfillmentStatus.DELIVERED)) {
            // 배송 시작 전인 상품만 취소 가능: 물품을 받은 다음 환불만 가능
            throw new ApplicationException(ApplicationError.ORDER_CANNOT_CANCEL);
        }
        /*

            pg 환불로직

         */

        // 차감한 재고 복구
        productService.rollbackInventory(orderItem.getProduct().getId(), orderItem.getQuantity());
        orderItem.updateFulfillmentStatus(FulfillmentStatus.CANCELED);
        orderItemFulFillmentHistoryService.updateFulfillmentCanceled(orderItem, false);
        orderItemRefundHistoryService.updateRefundRequested(orderItem);

        Integer notCanceledOrderItemCount = orderItemRepository.countNotCanceledOrderItemByOrderId(orderItem.getOrders().getId());
        if(notCanceledOrderItemCount == 0) {
            // 특정 주문의 모든 주문 요소가 취소되었다면 주문 자체의 상태도 '취소'로 설정
            orderItem.getOrders().setCanceled();
            orderStatusHistoryService.updatePaymentStatusCanceled(orderItem.getOrders());
        }
    }

    public ResOrderItemStatusHistoryDto getOrderItemStatusHistory(Long userId, Long orderItemId) {
        List<OrderItemFulfillmentHistory> fulfillmentHistoryList = orderItemFulfillmentHistoryRepository.findByOrderItemIdAndUserId(orderItemId, userId);
        List<OrderItemRefundHistory> refundHistoryList = orderItemRefundHistoryRepository.findByOrderItemIdAndUserId(orderItemId, userId);

        ResOrderItemStatusHistoryDto result = new ResOrderItemStatusHistoryDto();
        for(OrderItemFulfillmentHistory history : fulfillmentHistoryList) {
            result.getFulfillmentHistory().add(new ResFulfillmentStatusHistoryDto(history));
        }
        for(OrderItemRefundHistory history : refundHistoryList) {
            result.getRefundHistory().add(new ResRefundStatusHistoryDto(history));
        }
        return result;
    }

    /**
     * 판매자/양조장의 상품에 대한 주문 요소 정보를 조회
     * @param userId 판매자/양조장의 회원 식별자
     * @param startOffset
     * @return
     */
    public Page<ResOrderItemForSellerDto> getMyProductOrderList(Long userId, Integer startOffset) {
        if(startOffset == null || startOffset < 0) {
            startOffset = 0;
        }
        Pageable pageable = PageRequest.of(startOffset, ORDER_ITEM_PAGE_SIZE);
        Page<ResOrderItemForSellerDto> result = orderItemRepository.findByOrderIdListByProviderId(pageable, userId);
        if(result.getContent().isEmpty()) {
            throw new ApplicationException(ApplicationError.ORDER_ITEM_NOT_FOUND);
        }
        List<Long> orderItemList = result.stream().map(ResOrderItemForSellerDto::getOrder_item_id).toList();
        List<OrderItemFulfillmentHistory> fulfillmentHistoryList = orderItemFulfillmentHistoryRepository.findByOrderItemIdListAndProviderId(orderItemList, userId);
        List<OrderItemRefundHistory> refundHistoryList = orderItemRefundHistoryRepository.findByOrderItemIdListAndProviderId(orderItemList, userId);

        // 각 order_item status history 정보를 결과 dto의 필드에 삽입
        for(OrderItemFulfillmentHistory history : fulfillmentHistoryList) {
            for(ResOrderItemForSellerDto dto : result.getContent()) {
                if(dto.getOrder_item_id().equals(history.getOrderItem().getId())) {
                    dto.getStatus_history().getFulfillmentHistory().add(new ResFulfillmentStatusHistoryDto(history));
                    break;
                }
            }
        }
        for(OrderItemRefundHistory history : refundHistoryList) {
            for(ResOrderItemForSellerDto dto : result.getContent()) {
                if(dto.getOrder_item_id().equals(history.getOrderItem().getId())) {
                    dto.getStatus_history().getRefundHistory().add(new ResRefundStatusHistoryDto(history));
                    break;
                }
            }
        }
        return result;

    }

}
