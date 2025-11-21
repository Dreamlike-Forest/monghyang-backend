package com.example.monghyang.domain.joy.dto.slot;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class JoySlotTimeCountDto {
    // ResJoySlotTimeDto의 필드로 사용되는 dto 객체
    private LocalTime joy_slot_reservation_time; // 예약 시간대
    private Integer joy_slot_remaining_count; // 해당 시간대의 남은 자릿수

    private JoySlotTimeCountDto(LocalTime time, Integer remainingCount) {
        this.joy_slot_reservation_time = time;
        this.joy_slot_remaining_count = remainingCount;
    }

    public static JoySlotTimeCountDto timeCountOf(LocalTime time, Integer remainingCount) {
        return new JoySlotTimeCountDto(time, remainingCount);
    }
}
