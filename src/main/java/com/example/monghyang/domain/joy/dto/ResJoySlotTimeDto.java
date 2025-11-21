package com.example.monghyang.domain.joy.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ResJoySlotTimeDto {
    private LocalTime joy_slot_reservation_time; // 예약 시간대
    private Integer joy_slot_remaining_count; // 해당 시간대의 남은 자릿수
}
