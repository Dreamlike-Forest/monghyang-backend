package com.example.monghyang.domain.orders.item.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.orders.item.dto.ResOrderItemStatusHistoryDto;
import com.example.monghyang.domain.orders.item.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-item")
@RequiredArgsConstructor
@Tag(name = "주문 상품 API", description = "상품 주문 요소 취소 및 상태 변경 내역 API")
@SecurityRequirement(name = "SessionID")
public class OrderItemController {
    private final OrderItemService orderItemService;

    @PostMapping("/cancel/{orderItemId}")
    @Operation(summary = "해당 주문 요소 취소 처리", description = "PAID 상태의 주문의 주문요소를 취소할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 요소 취소 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "배송 시작 등으로 취소할 수 없는 주문 요소",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "주문 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> cancelOrderItem(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable("orderItemId") Long orderItemId) {
        orderItemService.cancelOrderItem(userId, orderItemId);
        return ResponseEntity.ok().body(ResponseDataDto.success("주문이 취소되었습니다."));
    }

    @GetMapping("/history/{orderItemId}")
    @Operation(summary = "주문 요소의 상태 변경 내역 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 요소 상태 변경 내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "주문 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<ResOrderItemStatusHistoryDto>> getOrderItemStatusHistory(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable("orderItemId") Long orderItemId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(orderItemService.getOrderItemStatusHistory(userId, orderItemId)));
    }
}
