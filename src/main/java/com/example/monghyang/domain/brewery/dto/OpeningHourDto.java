package com.example.monghyang.domain.brewery.dto;

import java.time.LocalTime;

public record OpeningHourDto(LocalTime startTime, LocalTime endTime) {}
