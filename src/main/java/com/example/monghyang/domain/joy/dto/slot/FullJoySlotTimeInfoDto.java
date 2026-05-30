package com.example.monghyang.domain.joy.dto.slot;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 예약 인원이 최대 수용 인원에 도달한 체험 슬롯의 날짜와 시간을 나타냅니다.
 */
public interface FullJoySlotTimeInfoDto {
    /**
     * 매진된 체험 예약일입니다.
     */
    LocalDate getReservationDate();

    /**
     * 매진된 체험 시작 시간입니다.
     */
    LocalTime getReservationTime();
}
