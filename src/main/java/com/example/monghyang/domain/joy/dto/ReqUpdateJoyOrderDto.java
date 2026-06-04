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
@Schema(description = "체험 예약 변경 요청 정보")
public class ReqUpdateJoyOrderDto {
    @Schema(description = "변경할 체험 예약 내역 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "수정하려는 양조장 체험 예약 내역 식별자를 입력하세요.")
    private Long id;
    @Schema(description = "변경할 예약 날짜입니다. yyyy-MM-dd 형식이며 null이면 변경하지 않습니다.", example = "2026-06-15", type = "string", format = "date", nullable = true)
    private LocalDate reservation_date;
    @Schema(description = "변경할 예약 시작 시간입니다. HH:mm:ss 형식이며 null이면 변경하지 않습니다.", example = "14:00:00", type = "string", format = "time", nullable = true)
    private LocalTime reservation_time;
    @Schema(description = "변경할 예약 인원입니다. null이면 변경하지 않습니다.", example = "2", minimum = "1", nullable = true)
    @Min(value = 1, message = "체험 인원은 1명 이상이어야 합니다.")
    private Integer count;
}
