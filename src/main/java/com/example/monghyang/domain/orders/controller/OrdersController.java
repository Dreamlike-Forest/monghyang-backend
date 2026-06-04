package com.example.monghyang.domain.orders.controller;

import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.global.order.ReqOrderDto;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.orders.dto.ReqPreOrderDto;
import com.example.monghyang.domain.orders.dto.ResOrderDto;
import com.example.monghyang.domain.orders.dto.ResOrderStatusHistoryDto;
import com.example.monghyang.domain.orders.service.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "상품 주문 API", description = "상품 장바구니 주문 및 결제 승인 API")
@SecurityRequirement(name = "SessionID")
public class OrdersController {
    private final OrdersService ordersService;

    @PostMapping("/prepare")
    @Operation(summary = "PG사로 전송할 'orderId' 값을 발급하기 위한 API", description = "프론트엔드에서 PG사로 결제 요청하기 전에 수행해주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 주문 결제용 orderId 발급 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "장바구니 수량, 상품 주문 가능 상태 또는 요청 금액이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원, 상품 또는 장바구니 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<UUID>> prepareOrderRequest(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute ReqPreOrderDto dto) {
        UUID orderId = ordersService.prepareOrder(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(orderId));
    }

    @PostMapping("/request")
    @Operation(summary = "PG사 결제 요청 이후 실제 결제 승인을 요청하는 API", description = "서버에서 실제 결제 승인 요청을 PG사로 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 주문 결제 승인 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "주문 금액 조작 또는 결제 승인 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 주문이 아닌 결제 승인 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "주문 또는 주문 요소가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "PG 결제 승인 또는 주문 상태 처리 중 서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> orderRequestToPG(@Parameter(hidden = true) @LoginUserId Long userId, @Valid @ModelAttribute ReqOrderDto dto) {
        try{
            ordersService.requestOrderToPG(userId, dto);
        } catch (Exception e){
            ordersService.setStatusFailed(dto.getPg_order_id());
            throw e;
        }
        return ResponseEntity.ok().body(ResponseDataDto.success("결제가 완료되었습니다."));
    }

    @GetMapping("/my/{startOffset}")
    @Operation(summary = "자신의 모든 주문 최신순 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 상품 주문 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResOrderDto>>> getMyOrderInfo(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Integer startOffset) {
        // 주문 테이블 조회 -> 그에 맞는 주문 요소 조회 후 필드에 매핑
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(ordersService.getMyOrderList(userId, startOffset)));
    }

    @GetMapping("/history/{orderId}")
    @Operation(summary = "자신의 특정 주문의 상태 변경 내역 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 주문 상태 변경 내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 주문이 아닌 주문 내역 조회 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "주문 또는 상태 변경 내역이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<List<ResOrderStatusHistoryDto>>> getOrderHistory(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long orderId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(ordersService.getMyOrderStatusHistory(userId, orderId)));
    }

}
