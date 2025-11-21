package com.example.monghyang.domain.joy.dto.slot;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResJoySlotTimeDto {
    private List<LocalTime> time_info = new ArrayList<>();
    private List<JoySlotTimeCountDto> remaining_count_list = new ArrayList<>();
}
