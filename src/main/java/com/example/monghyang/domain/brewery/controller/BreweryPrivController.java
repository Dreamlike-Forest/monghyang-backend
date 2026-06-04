package com.example.monghyang.domain.brewery.controller;

import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.brewery.dto.ReqClosedDateTimeDto;
import com.example.monghyang.domain.brewery.dto.ReqUpdateBreweryScheduleDto;
import com.example.monghyang.domain.global.advice.ApplicationErrorDto;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/brewery-priv") // 양조장용 api
@Tag(name = "양조장 관리자용 API", description = "양조장 권한을 가진 회원만 접근할 수 있습니다.")
@SecurityRequirement(name = "SessionID")
@RequiredArgsConstructor
public class BreweryPrivController {
    private final BreweryService breweryService;
    private final BreweryTagService breweryTagService;
    private final JoyService joyService;
    private final JoyOrderService joyOrderService;

    // 양조장 권한 검증: (@LoginUserId로 회원식별자 추출 -> 해당되는 양조장 조회 -> 양조장 식별자 사용)

    // 양조장 수정 로직(이미지 추가/삭제 또한 한번에 가능하도록)
    @PostMapping("/update")
    @Operation(
            summary = "양조장 정보 수정",
            description = "로그인한 양조장 회원의 양조장 기본 정보와 이미지 목록을 부분 수정합니다. null로 전달된 필드는 변경하지 않으며, 이미지 순서 seq=1이 대표 이미지입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 정보 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"양조장 정보를 업데이트했습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "이미지 개수, 이미지 순서, 입력값 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "자신의 이미지가 아닌 이미지 수정 또는 삭제 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 이미지 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateImageList(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 양조장 정보입니다. 이미지 추가/순서변경/삭제 필드는 multipart/form-data로 전달합니다.",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ReqUpdateBreweryDto.class))
            )
            @Valid @ModelAttribute ReqUpdateBreweryDto reqBreweryDto
    ) {
        breweryService.breweryUpdate(userId, reqBreweryDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보를 업데이트했습니다."));
    }

    // 양조장 삭제 처리
    @DeleteMapping
    @Operation(summary = "양조장 삭제 처리", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 삭제 처리 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "비밀번호 입력값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없거나 비밀번호가 일치하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> breweryQuit(
            @Valid @ModelAttribute VerifyAuthDto quitRequestDto,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        breweryService.breweryQuit(userId, quitRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보가 삭제되었습니다."));
    }

    @PostMapping("/restore")
    @Operation(summary = "양조장 복구", description = "해당 회원의 기존 비밀번호를 입력받고, 일치하는지 검사합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 복구 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "비밀번호 입력값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없거나 비밀번호가 일치하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> breweryRestore(
            @Valid @ModelAttribute VerifyAuthDto restoreRequestDto,
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        breweryService.breweryRestore(userId, restoreRequestDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("양조장 정보가 복구되었습니다."));
    }

    // 태그 추가 및 삭제
    @PostMapping("/tag")
    @Operation(summary = "양조장에 태그를 추가하거나 기존의 태그를 삭제합니다.", description = "추가 대상 태그 식별자 리스트와 삭제 대상 태그 식별자 리스트를 json으로 보내주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 태그 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "태그 수정 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 태그 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateTag(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @RequestBody ReqTagDto reqBreweryTagDto
    ) {
        breweryTagService.updateTag(userId, reqBreweryTagDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("태그 수정사항이 반영되었습니다."));
    }

    @GetMapping("/joy")
    @Operation(summary = "자신이 제공하는 체험 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 양조장 체험 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<List<ResJoyDto>>> getMyJoyList(
            @Parameter(hidden = true) @LoginUserId Long userId
    ) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyService.getMyJoyList(userId)));
    }

    @PostMapping("/joy-add")
    @Operation(
            summary = "체험 추가",
            description = "로그인한 양조장 회원이 새 체험을 등록합니다. 체험 생성 시 최초 요일별 시작 시간 스냅샷도 함께 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 추가 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"체험이 추가되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "가격, 인원, 일정, 이미지 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> createJoy(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "등록할 체험 정보입니다. image는 선택 파일이며, schedules는 요일별 예약 가능 시작 시간 목록입니다.",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ReqJoyDto.class))
            )
            @Valid @ModelAttribute ReqJoyDto reqJoyDto
    ) {
        joyService.createJoy(userId, reqJoyDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 추가되었습니다."));
    }

    @PostMapping("/joy-update")
    @Operation(summary = "체험 내용 수정", description = "가격, 할인율, 기타 체험 정보, 매진 처리 등")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 정보 수정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "체험 수정 입력값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "해당 체험을 수정할 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateJoy(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @Valid @ModelAttribute ReqUpdateJoyDto reqUpdateJoyDto
    ) {
        joyService.updateJoy(userId, reqUpdateJoyDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험정보가 수정되었습니다."));
    }

    @PostMapping("/joy/schedule")
    @Operation(
            summary = "체험 요일별 시작 시간 일정 변경",
            description = "effective_date부터 적용될 체험 시작 시간 스냅샷을 등록합니다. 같은 적용일의 기존 스냅샷은 요청 내용으로 교체되며, 목록에 없는 요일은 체험 미운영으로 해석합니다. 적용일 이후 PAID 상태 예약은 환불 요청 대상으로 전환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 일정 변경 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"체험 일정이 변경되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "effective_date가 과거이거나, 중복 요일/시간 또는 운영시간 밖 시작 시간이 포함됨",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateJoySchedule(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 체험 식별자, 적용 시작일, 요일별 시작 시간 목록입니다.",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ReqUpdateJoyScheduleDto.class))
            )
            @Valid @ModelAttribute ReqUpdateJoyScheduleDto dto
    ) {
        joyService.updateJoySchedule(userId, dto);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험 일정이 변경되었습니다."));
    }

    @DeleteMapping("/joy/{joyId}")
    @Operation(summary = "체험 삭제 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 삭제 처리 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "해당 체험을 삭제할 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteJoy(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long joyId
    ) {
        joyService.deleteJoy(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 삭제 처리되었습니다."));
    }

    @PostMapping("/joy-restore/{joyId}")
    @Operation(summary = "체험 복구")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 복구 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "해당 체험을 복구할 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> restoreJoy(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long joyId
    ) {
        joyService.restoreJoy(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 복구되었습니다."));
    }

    @PostMapping("/joy-set-soldout/{joyId}")
    @Operation(summary = "체험 품절처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 품절 처리 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "해당 체험을 품절 처리할 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> setSoldout(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long joyId
    ) {
        joyService.setSoldout(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 품절 처리되었습니다."));
    }

    @PostMapping("/joy-unset-soldout/{joyId}")
    @Operation(summary = "체험 품절 상태 복구")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 품절 상태 복구 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "해당 체험의 품절 상태를 복구할 권한이 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> unSetSoldout(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long joyId
    ) {
        joyService.unSetSoldout(userId, joyId);
        return ResponseEntity.ok().body(ResponseDataDto.success("체험이 품절 상태에서 복구되었습니다."));
    }

    @PostMapping("/joy-order/change")
    @Operation(summary = "체험 예약 시간대 변경 API", description = "다른 예약과 충돌하지 않으면 수정됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 예약 시간대 변경 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "예약 시간대 변경 요청값이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 예약 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "다른 예약과 시간이 충돌함",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> changeTime(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @ModelAttribute @Valid ReqUpdateJoyOrderDto reqUpdateJoyOrderDto
    ) {
        joyOrderService.updateReservationByBrewery(userId, reqUpdateJoyOrderDto);
        return ResponseEntity.ok().body(ResponseDataDto.success("예약 시간대 수정이 완료되었습니다."));
    }

    @DeleteMapping("/joy-order/{joyOrderId}")
    @Operation(summary = "체험 예약 내역 삭제 요청 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체험 예약 내역 삭제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 또는 체험 예약 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteHistory(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Long joyOrderId
    ) {
        joyOrderService.cancelByBrewery(userId, joyOrderId);
        return ResponseEntity.ok().body(ResponseDataDto.success("해당 체험 예약 삭제가 완료되었습니다."));
    }

    @GetMapping("/joy-order/history/{startOffset}")
    @Operation(summary = "자신의 양조장의 체험 예약 현황 및 내역 최신순 확인", description = "페이지 크기: 12")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 양조장 체험 예약 내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyOrderDto>>> getHistoryOfMyBrewery(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Integer startOffset
    ) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyOrderService.getHistoryOfMyBrewery(userId, startOffset)));
    }

    @GetMapping("/joy-order/history-date/{startOffset}/{date}")
    @Operation(summary = "자신의 양조장의 특정 날짜의 체험 예약 현황 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 양조장 특정 날짜 체험 예약 내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class))),
            @ApiResponse(responseCode = "400", description = "날짜 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Page<ResJoyOrderDto>>> getHistoryOfMyBreweryByDate(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @PathVariable Integer startOffset,
            @PathVariable LocalDate date
    ) {
        return ResponseEntity.ok().body(ResponseDataDto.contentFrom(joyOrderService.getHistoryOfMyBreweryByDate(userId, startOffset, date)));
    }

    @PostMapping("/brewery-close-try")
    @Operation(
            summary = "자신의 양조장의 '별도 휴무일' 지정 시도",
            description = "별도 휴무일을 PENDING 상태로 저장합니다. 이 API는 예약 목록을 반환하지 않으며, 신규 예약 차단과 기존 예약 환불 요청은 확정 API 호출 후 수행됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "별도 휴무일 PENDING 저장 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"별도 휴무일이 설정되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "closed_date가 과거이거나 날짜 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "이미 휴무 처리된 날짜",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> tryBreweryClosedDate(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "PENDING 상태로 저장할 별도 휴무일 정보입니다. 현재 구현은 closed_date와 reason을 사용합니다.",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ReqClosedDateTimeDto.class))
            )
            @Valid @ModelAttribute ReqClosedDateTimeDto dto
    ) {
        // 해당 날짜를 휴무일 후보로 저장하고, 'PENDING' 상태로 둔다.
        // 신규 예약 차단과 기존 예약 환불 요청은 휴무일이 'CONFIRMED'로 확정된 뒤 수행한다.
        breweryService.addClosedDateTry(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("별도 휴무일이 설정되었습니다."));
    }

    @PostMapping("/brewery-close-confirmed")
    @Operation(
            summary = "자신의 양조장의 '별도 휴무일' 지정 확정",
            description = "PENDING 상태로 저장된 별도 휴무일을 CONFIRMED 상태로 변경합니다. 확정 시 해당 날짜의 PAID 체험 예약은 REFUND_REQUESTED 상태로 전환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "별도 휴무일 확정 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"별도 휴무일이 확정되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "closed_date가 과거이거나 PENDING 휴무일이 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> confirmedBreweryClosedDate(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "확정할 별도 휴무일 정보입니다. 현재 구현은 closed_date로 PENDING 휴무일을 찾습니다.",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ReqClosedDateTimeDto.class))
            )
            @Valid @ModelAttribute ReqClosedDateTimeDto dto
    ) {
        // API 요청을 받으면 체험 예약 일괄 취소만 수행('REFUND_REQUESTED' 상태로 일괄 변경)
        // 이후의 실제 환불절차는 '스케줄러'를 통해 주기적으로 수행(트랜잭션이 적용되지 않은 스케줄링 메서드에서 여러 개의 트랜잭션 메서드 호출)
        breweryService.addClosedDateConfirmed(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("별도 휴무일이 확정되었습니다."));
    }

    @DeleteMapping("/brewery-close")
    @Operation(
            summary = "자신의 양조장의 '별도 휴무일' 해제",
            description = "closed_date에 해당하는 별도 휴무일을 삭제합니다. 현재 구현은 closed_time과 reason을 삭제 조건으로 사용하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "별도 휴무일 해제 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"별도 휴무일이 해제되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "closed_date가 과거이거나 날짜 형식이 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "휴무일이 아닌 날짜",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> deleteBreweryClosedDate(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @ParameterObject @Valid @ModelAttribute ReqClosedDateTimeDto dto
    ) {
        breweryService.deleteClosedDate(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("별도 휴무일이 해제되었습니다."));
    }

    @PostMapping("/schedule")
    @Operation(
            summary = "양조장 운영시간/휴게시간 일정 변경",
            description = "effective_date(적용 시작일)부터 적용될 요일별 운영시간/휴게시간 스냅샷을 등록합니다. "
                    + "effective_date 이후에 예약된 PAID 상태의 체험 예약은 자동으로 환불 처리됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "양조장 일정 변경 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDataDto.class),
                            examples = @ExampleObject(value = "{\"status\":200,\"message\":\"양조장 일정이 변경되었습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "effective_date가 과거이거나, 중복 요일 또는 운영/휴게시간 범위가 올바르지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "세션 인증 정보가 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "양조장 정보가 존재하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorDto.class)))
    })
    public ResponseEntity<ResponseDataDto<Void>> updateBrewerySchedule(
            @Parameter(hidden = true) @LoginUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "적용 시작일과 요일별 운영/휴게시간 전체 스냅샷입니다. 목록에 없는 요일은 미운영으로 해석합니다.",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = ReqUpdateBreweryScheduleDto.class))
            )
            @Valid @ModelAttribute ReqUpdateBreweryScheduleDto dto
    ) {
        breweryService.updateBrewerySchedule(userId, dto);
        return ResponseEntity.ok(ResponseDataDto.success("양조장 일정이 변경되었습니다."));
    }

}
