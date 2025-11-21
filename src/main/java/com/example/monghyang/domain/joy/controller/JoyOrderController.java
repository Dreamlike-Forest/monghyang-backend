package com.example.monghyang.domain.joy.controller;

import com.example.monghyang.domain.joy.dto.*;
import com.example.monghyang.domain.global.order.ReqOrderDto;
import com.example.monghyang.domain.joy.service.JoyOrderService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.joy.service.JoySlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/joy-order")
@Tag(name = "체험 예약 API")
public class JoyOrderController {
    private final JoyOrderService joyOrderService;
    private final JoySlotService joySlotService;

    @Autowired
    public JoyOrderController(JoyOrderService joyOrderService, JoySlotService joySlotService) {
        this.joyOrderService = joyOrderService;
        this.joySlotService = joySlotService;
    }

    @GetMapping("/my/{startOffset}")
    @Operation(summary = "자신의 체험 예약 내역 조회", description = "페이지 크기: 12")
    public ResponseEntity<ResponseDataDto<Page<ResJoyOrderDto>>> getMyOrders(@LoginUserId Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyOrderService.getHistoryOfUser(userId, startOffset)));
    }

    @GetMapping("/calendar")
    @Operation(summary = "특정 Month의 예약 불가능한 날 조회", description = "모든 파라미터 필수")
    public ResponseEntity<ResponseDataDto<ResJoySlotDateDto>> getImpossibleDate(@Valid ReqFindJoySlotDateDto dto) {
        if(dto.getMonth() > 12) {
            dto.setMonth(12);
        } else if(dto.getMonth() < 1) {
            dto.setMonth(1);
        }
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joySlotService.getImpossibleDate(dto)));
    }

    // 체험 예약 요청, uuid를 클라이언트로 반환
    @PostMapping("/prepare")
    @Operation(summary = "PG사로 전송할 'orderId' 값을 발급하기 위한 API", description = "프론트엔드에서 PG사로 결제 요청하기 전에 수행해주세요.")
    public ResponseEntity<ResponseDataDto<UUID>> prepareOrderRequest(@LoginUserId Long userId, @ModelAttribute @Valid ReqJoyPreOrderDto dto) {
        // 1. 예약 슬롯 확보
        joyOrderService.incrementJoySlotCount(dto.getId(), dto.getReservation_date(), dto.getReservation_time(), dto.getCount());
        // 2. pgOrderId 발급
        UUID pgOrderId = joyOrderService.prepareOrder(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(pgOrderId));
    }

    // PG사로 결제 승인 요청
    @PostMapping("/request")
    @Operation(summary = "PG사 결제 요청 이후 실제 결제 승인을 요청하는 API", description = "서버에서 실제 결제 승인 요청을 PG사로 전송합니다.")
    public ResponseEntity<ResponseDataDto<Void>> orderRequestToPG(@LoginUserId Long userId, @ModelAttribute @Valid ReqOrderDto reqOrderDto) {
        joyOrderService.requestOrderToPG(userId, reqOrderDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("결제가 완료되었습니다."));
    }

    // 체험 시간 변경 요청(예약 전날까지만 가능)
    @PostMapping("/change")
    @Operation(summary = "체험 예약 시간대 변경 API", description = "예약 전날까지만 수행 가능, 다른 예약과 충돌하지 않으면 수정됩니다.")
    public ResponseEntity<ResponseDataDto<Void>> changeTime(@LoginUserId Long userId, @ModelAttribute @Valid ReqUpdateJoyOrderDto reqUpdateJoyOrderDto) {
        joyOrderService.updateReservation(userId, reqUpdateJoyOrderDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 시간대 수정이 완료되었습니다."));
    }

    // 체험 취소(환불) 요청(예약 전날까지만 가능)
    @DeleteMapping("/cancel/{joyOrderId}")
    @Operation(summary = "체험 취소(환불) 요청 API", description = "예약 전날까지만 가능(세부적인 취소 수수료 설정은 추후 구현)")
    public ResponseEntity<ResponseDataDto<Void>> cancelOrder(@LoginUserId Long userId, @PathVariable Long joyOrderId) {
        joyOrderService.cancel(userId, joyOrderId);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 취소가 완료되었습니다."));
    }

    // 체험 예약 내역 삭제 처리(체험 종료 이후에만 가능)
    @DeleteMapping("/history/{joyOrderId}")
    @Operation(summary = "체험 예약 내역 삭제 요청 API", description = "취소된 예약 내역이거나 체험이 종료된 내역에 한해서 수행 가능")
    public ResponseEntity<ResponseDataDto<Void>> deleteHistory(@LoginUserId Long userId, @PathVariable Long joyOrderId) {
        joyOrderService.deleteHistory(userId, joyOrderId);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 내역 삭제가 완료되었습니다."));
    }
}
