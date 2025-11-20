package com.example.monghyang.domain.brewery.dto;

import java.time.LocalTime;

public record JoyInfoDto(LocalTime breweryStartTime, LocalTime breweryEndTime, Integer timeUnit, Integer maxCount) {}
