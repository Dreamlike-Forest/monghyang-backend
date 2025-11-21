package com.example.monghyang.domain.joy.dto;

import java.time.LocalTime;

public interface JoyScheduleCountDto {
    // 해당 체험이 하루에 몇 회 운영되는지 계산하기 위한 데이터 조회에 사용되는 dto
    Integer getTimeUnit();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
