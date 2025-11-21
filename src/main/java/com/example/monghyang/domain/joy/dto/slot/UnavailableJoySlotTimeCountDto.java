package com.example.monghyang.domain.joy.dto.slot;

import java.time.LocalDate;

public interface UnavailableJoySlotTimeCountDto {
    // 일별 예약 불가 시간대 개수를 집계한 결과를 매핑하는 dto
    LocalDate getReservationDate();
    Integer getCount();
}
