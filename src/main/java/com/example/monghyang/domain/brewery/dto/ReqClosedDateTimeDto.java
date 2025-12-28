package com.example.monghyang.domain.brewery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReqClosedDateTimeDto {
    @NotNull(message = "별도 휴무일 날짜 정보를 입력해주세요.")
    private LocalDate closed_date;
    private LocalTime closed_time; // not required
    private String reason;
}
