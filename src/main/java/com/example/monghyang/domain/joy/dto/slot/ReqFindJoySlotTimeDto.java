package com.example.monghyang.domain.joy.dto.slot;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReqFindJoySlotTimeDto {
    @NotNull(message = "체험 식별자를 입력해주세요.")
    private Long joyId;
    @NotNull(message = "시간대를 조회하려는 날짜 정보를 입력해주세요.")
    private LocalDate date;
}
