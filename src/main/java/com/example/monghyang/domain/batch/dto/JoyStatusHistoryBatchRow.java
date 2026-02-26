package com.example.monghyang.domain.batch.dto;

import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JoyStatusHistoryBatchRow {
    private final Long joyOrderId;
    private final JoyPaymentStatus toStatus;
    private final String reasonCode;
    private final LocalDateTime createdAt;
    public JoyStatusHistoryBatchRow(Long joyOrderId, JoyPaymentStatus toStatus, String reasonCode) {
        this.joyOrderId = joyOrderId;
        this.toStatus = toStatus;
        this.reasonCode = reasonCode;
        this.createdAt = LocalDateTime.now();
    }
}
