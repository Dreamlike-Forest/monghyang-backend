package com.example.monghyang.domain.joy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "체험 예약 결제 준비 요청 정보")
public class ReqJoyPreOrderDto {
    @Schema(description = "예약할 체험 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "체험 식별자를 입력해주세요.")
    private Long id;
    @Schema(description = "예약 인원입니다. 1명 이상이어야 합니다.", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 인원 정보를 입력해주세요.")
    @Min(value = 1, message = "체험 인원은 1명 이상이어야 합니다.")
    private Integer count;
    @Schema(description = "예약자명입니다.", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약자명을 입력해주세요.")
    private String payer_name;
    @Schema(description = "예약자 연락처입니다.", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약자 전화번호를 입력해주세요.")
    private String payer_phone;
    @Schema(description = "예약 날짜입니다. yyyy-MM-dd 형식입니다.", example = "2026-06-15", type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 날짜를 입력해주세요.")
    private LocalDate reservation_date;
    @Schema(description = "예약 시작 시간입니다. HH:mm:ss 형식입니다.", example = "14:00:00", type = "string", format = "time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "예약 시간대를 입력해주세요.")
    private LocalTime reservation_time;
}
