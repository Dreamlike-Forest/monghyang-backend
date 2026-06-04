package com.example.monghyang.domain.joy.controller;

import com.example.monghyang.domain.joy.dto.*;
import com.example.monghyang.domain.global.order.ReqOrderDto;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
import com.example.monghyang.domain.joy.dto.slot.ReqFindJoySlotDateDto;
import com.example.monghyang.domain.joy.dto.slot.ReqFindJoySlotTimeDto;
import com.example.monghyang.domain.joy.dto.slot.ResJoySlotDateDto;
import com.example.monghyang.domain.joy.dto.slot.ResJoySlotTimeDto;
import com.example.monghyang.domain.joy.service.JoyOrderService;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import com.example.monghyang.domain.joy.service.JoySlotService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/joy-order")
@Tag(name = "체험 예약 API")
@RequiredArgsConstructor
public class JoyOrderController {
    private final JoyOrderService joyOrderService;
    private final JoySlotService joySlotService;

    @GetMapping("/my/{startOffset}")
    @Operation(summary = "자신의 체험 예약 내역 조회", description = "페이지 크기: 12", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 체험 예약 내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 또는 체험 예약 내역이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyOrderDto>>> getMyOrders(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyOrderService.getHistoryOfUser(userId, startOffset)));
    }

    @GetMapping("/calendar")
    @Operation(summary = "특정 Month의 예약 불가능한 날 조회", description = "모든 파라미터 필수")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 불가능 날짜 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "체험 식별자, 연도 또는 월 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<ResJoySlotDateDto>> getImpossibleDate(@Valid ReqFindJoySlotDateDto dto) {
        if(dto.getMonth() > 12) {
            dto.setMonth(12);
        } else if(dto.getMonth() < 1) {
            dto.setMonth(1);
        }
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joySlotService.getImpossibleDate(dto)));
    }

    @GetMapping("/calendar/time-info")
    @Operation(summary = "특정 날의 모든 시간대의 '남아있는 자릿수' 정보 조회", description = "남아있는 자릿수가 0이라면 예약 불가를 의미")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 가능 시간과 잔여 좌석 수 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "체험 식별자 또는 날짜 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<ResJoySlotTimeDto>> getRemainingCountList(@Valid ReqFindJoySlotTimeDto dto) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joySlotService.getRemainingCountList(dto.getJoyId(), dto.getDate())));
    }

    // 체험 예약 요청, uuid를 클라이언트로 반환
    @PostMapping("/prepare")
    @Operation(summary = "PG사로 전송할 'orderId' 값을 발급하기 위한 API", description = "프론트엔드에서 PG사로 결제 요청하기 전에 수행해주세요.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 예약 결제용 orderId 발급 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "예약 인원 초과, 유효하지 않은 체험 시간대 또는 요청값 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "회원, 양조장 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "선택한 체험 시간대에 이미 다른 예약이 존재함",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<UUID>> prepareOrderRequest(@Parameter(hidden = true) @LoginUserId Long userId, @ModelAttribute @Valid ReqJoyPreOrderDto dto) {
        // 1. 예약 슬롯 확보
        joyOrderService.reservationJoySlotCount(dto.getId(), dto.getReservation_date(), dto.getReservation_time(), dto.getCount());
        // 2. pgOrderId 발급
        UUID pgOrderId = joyOrderService.prepareOrder(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(pgOrderId));
    }

    // PG사로 결제 승인 요청
    @PostMapping("/request")
    @Operation(summary = "PG사 결제 요청 이후 실제 결제 승인을 요청하는 API", description = "서버에서 실제 결제 승인 요청을 PG사로 전송합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 예약 결제 승인 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "주문 금액 조작 또는 결제 승인 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 체험 예약 주문이 아닌 결제 승인 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 예약 주문 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "PG 결제 승인 또는 예약 주문 상태 처리 중 서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> orderRequestToPG(@Parameter(hidden = true) @LoginUserId Long userId, @ModelAttribute @Valid ReqOrderDto reqOrderDto) {
        try{
            joyOrderService.requestOrderToPG(userId, reqOrderDto);
        } catch (Exception e){
            joyOrderService.setStatusFailed(reqOrderDto.getPg_order_id());
            throw e;
        }
        return ResponseEntity.ok().body(ResponseDataDto.success("결제가 완료되었습니다."));
    }

    // 체험 시간 변경 요청(예약 전날까지만 가능)
    @PostMapping("/change")
    @Operation(summary = "체험 예약 시간대 변경 API", description = "예약 전날까지만 수행 가능, 다른 예약과 충돌하지 않으면 수정됩니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 예약 변경 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "예약 변경 가능 시점이 지났거나 요청 시간대가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 체험 예약이 아닌 변경 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 예약 내역 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "변경하려는 체험 시간대에 이미 다른 예약이 존재함",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> changeTime(@Parameter(hidden = true) @LoginUserId Long userId, @ModelAttribute @Valid ReqUpdateJoyOrderDto reqUpdateJoyOrderDto) {
        joyOrderService.updateReservation(userId, reqUpdateJoyOrderDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 시간대 수정이 완료되었습니다."));
    }

    // 체험 취소(환불) 요청(예약 전날까지만 가능)
    @DeleteMapping("/cancel/{joyOrderId}")
    @Operation(summary = "체험 취소(환불) 요청 API", description = "예약 전날까지만 가능합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 예약 취소 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "예약 취소 가능 시점이 지남",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 체험 예약이 아닌 취소 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 예약 내역이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> cancelOrder(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long joyOrderId) {
        joyOrderService.cancel(userId, joyOrderId);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 취소가 완료되었습니다."));
    }

    // 체험 예약 내역 삭제 처리(체험 종료 이후에만 가능)
    @DeleteMapping("/history/{joyOrderId}")
    @Operation(summary = "체험 예약 내역 삭제 요청 API", description = "취소된 예약 내역이거나 체험이 종료된 내역에 한해서 수행 가능합니다.", security = @SecurityRequirement(name = "SessionID"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 예약 내역 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "삭제할 수 없는 체험 예약 상태",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 체험 예약이 아닌 삭제 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "체험 예약 내역이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteHistory(@Parameter(hidden = true) @LoginUserId Long userId, @PathVariable Long joyOrderId) {
        joyOrderService.deleteHistory(userId, joyOrderId);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 내역 삭제가 완료되었습니다."));
    }
}
