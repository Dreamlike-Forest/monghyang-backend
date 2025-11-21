package com.example.monghyang.domain.joy.dto.slot;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResJoySlotDateDto {
    private List<LocalDate> joy_unavailable_reservation_date = new ArrayList<>();
}
