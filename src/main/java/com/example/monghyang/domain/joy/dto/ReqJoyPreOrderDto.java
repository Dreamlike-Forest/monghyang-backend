package com.example.monghyang.domain.joy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReqJoyPreOrderDto {
    @NotNull(message = "체험 식별자를 입력해주세요.")
    private Long id;
    @NotNull(message = "체험 인원 정보를 입력해주세요.")
    @Min(value = 1, message = "체험 인원은 1명 이상이어야 합니다.")
    private Integer count;
    @NotNull(message = "예약자명을 입력해주세요.")
    private String payer_name;
    @NotNull(message = "예약자 전화번호를 입력해주세요.")
    private String payer_phone;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @NotNull(message = "체험 날짜 및 시간대를 입력해주세요.(30분의 배수)")
    private LocalDateTime reservation;
}
