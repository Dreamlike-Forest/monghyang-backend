package com.example.monghyang.domain.brewery.controller;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.brewery.dto.ReqClosedDateTimeDto;
import com.example.monghyang.domain.brewery.dto.ReqUpdateBreweryScheduleDto;
import com.example.monghyang.domain.joy.dto.*;
import com.example.monghyang.domain.joy.service.JoyOrderService;
import com.example.monghyang.domain.joy.service.JoyService;
import com.example.monghyang.domain.brewery.dto.ReqUpdateBreweryDto;
import com.example.monghyang.domain.brewery.tag.BreweryTagService;
import com.example.monghyang.domain.tag.dto.ReqTagDto;
import com.example.monghyang.domain.global.annotation.auth.LoginUserId;
import com.example.monghyang.domain.brewery.service.BreweryService;
import com.example.monghyang.domain.global.response.ResponseDataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/brewery-priv") // 양조장용 api
@Tag(name = "양조장 관리자용 API", description = "양조장 권한을 가진 회원만 접근할 수 있습니다.")
@RequiredArgsConstructor
public class BreweryPrivController {
    private final BreweryService breweryService;
    private final BreweryTagService breweryTagService;
    private final JoyService joyService;
    private final JoyOrderService joyOrderService;

    // 양조장 권한 검증: (@LoginUserId로 회원식별자 추출 -> 해당되는 양조장 조회 -> 양조장 식별자 사용)

    // 양조장 수정 로직(이미지 추가/삭제 또한 한번에 가능하도록)
    @PostMapping("/update")
    @Operation(summary = "양조장 정보 수정(첫번째 이미지: 대표 이미지")
    public ResponseEntity<ResponseDataDto<Void>> updateImageList(@LoginUserId Long userId, @Valid @ModelAttribute ReqUpdateBreweryDto reqBreweryDto) {
        breweryService.breweryUpdate(userId, reqBreweryDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보를 업데이트했습니다."));
    }

    // 양조장 삭제 처리
    @DeleteMapping
    @Operation(summary = "양조장 삭제 처리", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    public ResponseEntity<ResponseDataDto<Void>> breweryQuit(@Valid @ModelAttribute VerifyAuthDto quitRequestDto, @LoginUserId Long userId) {
        breweryService.breweryQuit(userId, quitRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보가 삭제되었습니다."));
    }

    @PostMapping("/restore")
    @Operation(summary = "양조장 복구", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    public ResponseEntity<ResponseDataDto<Void>> breweryRestore(@Valid @ModelAttribute VerifyAuthDto restoreRequestDto, @LoginUserId Long userId) {
        breweryService.breweryRestore(userId, restoreRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보가 복구되었습니다."));
    }

    // 태그 추가 및 삭제
    @PostMapping("/tag")
    @Operation(summary = "양조장에 태그를 추가하거나 기존의 태그를 삭제합니다.", description = "추가 대상 태그 식별자 리스트와 삭제 대상 태그 식별자 리스트를 json으로 보내주세요.")
    public ResponseEntity<ResponseDataDto<Void>> updateTag(@LoginUserId Long userId, @RequestBody ReqTagDto reqBreweryTagDto) {
        breweryTagService.updateTag(userId, reqBreweryTagDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("태그 수정사항이 반영되었습니다."));
    }

    @GetMapping("/joy")
    @Operation(summary = "자신이 제공하는 체험 정보 조회")
    public ResponseEntity<ResponseDataDto<List<ResJoyDto>>> getMyJoyList(@LoginUserId Long userId) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyService.getMyJoyList(userId)));
    }

    @PostMapping("/joy-add")
    @Operation(summary = "체험 추가")
    public ResponseEntity<ResponseDataDto<Void>> createJoy(@LoginUserId Long userId, @Valid @ModelAttribute ReqJoyDto reqJoyDto) {
        joyService.createJoy(userId, reqJoyDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 추가되었습니다."));
    }

    @PostMapping("/joy-update")
    @Operation(summary = "체험 내용 수정", description = "가격, 할인율, 기타 체험 정보, 매진 처리 등")
    public ResponseEntity<ResponseDataDto<Void>> updateJoy(@LoginUserId Long userId, @Valid @ModelAttribute ReqUpdateJoyDto reqUpdateJoyDto) {
        joyService.updateJoy(userId, reqUpdateJoyDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험정보가 수정되었습니다."));
    }

    @DeleteMapping("/joy/{joyId}")
    @Operation(summary = "체험 삭제 처리")
    public ResponseEntity<ResponseDataDto<Void>> deleteJoy(@LoginUserId Long userId, @PathVariable Long joyId) {
        joyService.deleteJoy(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 삭제 처리되었습니다."));
    }

    @PostMapping("/joy-restore/{joyId}")
    @Operation(summary = "체험 복구")
    public ResponseEntity<ResponseDataDto<Void>> restoreJoy(@LoginUserId Long userId, @PathVariable Long joyId) {
        joyService.restoreJoy(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 복구되었습니다."));
    }

    @PostMapping("/joy-set-soldout/{joyId}")
    @Operation(summary = "체험 품절처리")
    public ResponseEntity<ResponseDataDto<Void>> setSoldout(@LoginUserId Long userId, @PathVariable Long joyId) {
        joyService.setSoldout(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 품절 처리되었습니다."));
    }

    @PostMapping("/joy-unset-soldout/{joyId}")
    @Operation(summary = "체험 품절 상태 복구")
    public ResponseEntity<ResponseDataDto<Void>> unSetSoldout(@LoginUserId Long userId, @PathVariable Long joyId) {
        joyService.unSetSoldout(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 품절 상태에서 복구되었습니다."));
    }

    @PostMapping("/joy-order/change")
    @Operation(summary = "체험 예약 시간대 변경 API", description = "다른 예약과 충돌하지 않으면 수정됩니다.")
    public ResponseEntity<ResponseDataDto<Void>> changeTime(@LoginUserId Long userId, @ModelAttribute @Valid ReqUpdateJoyOrderDto reqUpdateJoyOrderDto) {
        joyOrderService.updateReservationByBrewery(userId, reqUpdateJoyOrderDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 시간대 수정이 완료되었습니다."));
    }

    @DeleteMapping("/joy-order/{joyOrderId}")
    @Operation(summary = "체험 예약 내역 삭제 요청 API")
    public ResponseEntity<ResponseDataDto<Void>> deleteHistory(@LoginUserId Long userId, @PathVariable Long joyOrderId) {
        joyOrderService.cancelByBrewery(userId, joyOrderId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 체험 예약 삭제가 완료되었습니다."));
    }

    @GetMapping("/joy-order/history/{startOffset}")
    @Operation(summary = "자신의 양조장의 체험 예약 현황 및 내역 최신순 확인", description = "페이지 크기: 12")
    public ResponseEntity<ResponseDataDto<Page<ResJoyOrderDto>>> getHistoryOfMyBrewery(@LoginUserId Long userId, @PathVariable Integer startOffset) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyOrderService.getHistoryOfMyBrewery(userId, startOffset)));
    }

    @GetMapping("/joy-order/history-date/{startOffset}/{date}")
    @Operation(summary = "자신의 양조장의 특정 날짜의 체험 예약 현황 확인")
    public ResponseEntity<ResponseDataDto<Page<ResJoyOrderDto>>> getHistoryOfMyBreweryByDate(@LoginUserId Long userId, @PathVariable Integer startOffset, @PathVariable LocalDate date) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyOrderService.getHistoryOfMyBreweryByDate(userId, startOffset, date)));
    }

    @PostMapping("/brewery-close-try")
    @Operation(summary = "자신의 양조장의 '별도 휴무일' 지정 시도", description = "휴무일 지정으로 인해 취소되는 체험 예약의 리스트를 반환합니다.")
    public ResponseEntity<ResponseDataDto<Void>> tryBreweryClosedDate(@LoginUserId Long userId, @Valid @ModelAttribute ReqClosedDateTimeDto dto) {
        // 해당 날짜를 휴무일로 지정하고, 'PENDING' 상태로 설정
        // 이때는 신규 예약만 차단하고, 아직 환불 절차는 수행하지 않는 단계
        breweryService.addClosedDateTry(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("별도 휴무일이 설정되었습니다."));
    }

    @PostMapping("/brewery-close-confirmed")
    @Operation(summary = "자신의 양조장의 '별도 휴무일' 지정 확정", description = "양조장 측이 '환불 영향'을 모두 확인한 뒤 '휴무일 지정'을 확정하기 위해 사용하는 API")
    public ResponseEntity<ResponseDataDto<Void>> confirmedBreweryClosedDate(@LoginUserId Long userId, @Valid @ModelAttribute ReqClosedDateTimeDto dto) {
        // API 요청을 받으면 체험 예약 일괄 취소만 수행('REFUND_REQUESTED' 상태로 일괄 변경)
        // 이후의 실제 환불절차는 '스케줄러'를 통해 주기적으로 수행(트랜잭션이 적용되지 않은 스케줄링 메서드에서 여러 개의 트랜잭션 메서드 호출)
        breweryService.addClosedDateConfirmed(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("별도 휴무일이 확정되었습니다."));
    }

    @DeleteMapping("/brewery-close")
    @Operation(summary = "자신의 양조장의 '별도 휴무일' 해제")
    public ResponseEntity<ResponseDataDto<Void>> deleteBreweryClosedDate(@LoginUserId Long userId, @Valid @ModelAttribute ReqClosedDateTimeDto dto) {
        breweryService.deleteClosedDate(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("별도 휴무일이 해제되었습니다."));
    }

    @PostMapping("/schedule")
    @Operation(
            summary = "양조장 운영시간/휴게시간 일정 변경",
            description = "effective_date(적용 시작일)부터 적용될 요일별 운영시간/휴게시간 스냅샷을 등록합니다. "
                    + "effective_date 이후에 예약된 PAID 상태의 체험 예약은 자동으로 환불 처리됩니다."
    )
    public ResponseEntity<ResponseDataDto<Void>> updateBrewerySchedule(
            @LoginUserId Long userId,
            @Valid @ModelAttribute ReqUpdateBreweryScheduleDto dto
    ) {
        breweryService.updateBrewerySchedule(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("양조장 일정이 변경되었습니다."));
    }

}
