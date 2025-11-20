package com.example.monghyang.domain.joy.dto;

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
public class ReqUpdateJoyOrderDto {
    @NotNull(message = "수정하려는 양조장 체험 예약 내역 식별자를 입력하세요.")
    private Long id;
    private LocalDate reservation_date;
    private LocalTime reservation_time;
    @Min(value = 1, message = "체험 인원은 1명 이상이어야 합니다.")
    private Integer count;
}
