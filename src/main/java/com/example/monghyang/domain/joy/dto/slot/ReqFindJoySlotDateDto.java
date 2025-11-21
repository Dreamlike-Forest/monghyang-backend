package com.example.monghyang.domain.joy.dto.slot;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqFindJoySlotDateDto {
    @NotNull(message = "체험 식별자를 입력해주세요.")
    private Long joyId;
    @NotNull(message = "년도 정보를 입력해주세요.")
    private Integer year;
    @NotNull(message = "월 정보를 입력해주세요.")
    private Integer month;
}
