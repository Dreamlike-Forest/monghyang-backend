package com.example.monghyang.domain.orders.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.order.ReqOrderDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.orders.dto.ReqPreOrderDto;
import com.example.monghyang.domain.orders.dto.ResOrderDto;
import com.example.monghyang.domain.orders.dto.ResOrderStatusHistoryDto;
import com.example.monghyang.domain.orders.service.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrdersService ordersService;
    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @PostMapping("/prepare")
    @Operation(summary = "PG사로 전송할 'orderId' 값을 발급하기 위한 API", description = "프론트엔드에서 PG사로 결제 요청하기 전에 수행해주세요.")
    public ResponseEntity<ResponseDataDto<UUID>> prepareOrderRequest(@LoginUserId Long userId, @Valid @ModelAttribute ReqPreOrderDto dto) {
        UUID orderId = ordersService.prepareOrder(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(orderId));
    }

    @PostMapping("/request")
    @Operation(summary = "PG사 결제 요청 이후 실제 결제 승인을 요청하는 API", description = "서버에서 실제 결제 승인 요청을 PG사로 전송합니다.")
    public ResponseEntity<ResponseDataDto<Void>> orderRequestToPG(@LoginUserId Long userId, @Valid @ModelAttribute ReqOrderDto dto) {
        ordersService.requestOrderToPG(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.success("결제가 완료되었습니다."));
    }

    @GetMapping("/my/{startOffset}")
    @Operation(summary = "자신의 모든 주문 최신순 조회")
    public ResponseEntity<ResponseDataDto<Page<ResOrderDto>>> getMyOrderInfo(@LoginUserId Long userId, @PathVariable Integer startOffset) {
        // 주문 테이블 조회 -> 그에 맞는 주문 요소 조회 후 필드에 매핑
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(ordersService.getMyOrderList(userId, startOffset)));
    }

    @GetMapping("/history/{orderId}")
    @Operation(summary = "자신의 특정 주문의 상태 변경 내역 조회")
    public ResponseEntity<ResponseDataDto<List<ResOrderStatusHistoryDto>>> getOrderHistory(@LoginUserId Long userId, @PathVariable Long orderId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(ordersService.getMyOrderStatusHistory(userId, orderId)));
    }

}
