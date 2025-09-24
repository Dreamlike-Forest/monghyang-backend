package com.example.monghyang.domain.brewery.joy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReqUpdateJoyOrderDto {
    @NotNull(message = "수정하려는 양조장 체험 예약 내역 식별자를 입력하세요.")
    private Long id;
    private LocalDateTime reservation;
}
