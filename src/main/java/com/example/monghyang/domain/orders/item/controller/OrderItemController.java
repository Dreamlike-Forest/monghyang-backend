package com.example.monghyang.domain.orders.item.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemStatusHistoryDto;
import com.example.monghyang.domain.orders.item.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-item")
public class OrderItemController {
    private final OrderItemService orderItemService;
    @Autowired
    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @PostMapping("/cancel/{orderItemId}")
    @Operation(summary = "해당 주문 요소 취소 처리", description = "PAID 상태의 주문의 주문요소를 취소할 수 있습니다.")
    public ResponseEntity<ResponseDataDto<Void>> cancelOrderItem(@LoginUserId Long userId, @PathVariable("orderItemId") Long orderItemId) {
        orderItemService.cancelOrderItem(userId, orderItemId);
        return ResponseEntity.ok().body(ResponseDataDto.success("주문이 취소되었습니다."));
    }

    @GetMapping("/history/{orderItemId}")
    @Operation(summary = "주문 요소의 상태 변경 내역 조회")
    public ResponseEntity<ResponseDataDto<ResOrderItemStatusHistoryDto>> getOrderItemStatusHistory(@LoginUserId Long userId, @PathVariable("orderItemId") Long orderItemId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(orderItemService.getOrderItemStatusHistory(userId, orderItemId)));
    }
}
