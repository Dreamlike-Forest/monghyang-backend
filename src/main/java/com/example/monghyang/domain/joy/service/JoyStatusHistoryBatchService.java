package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JoyStatusHistoryBatchService {
    private final JdbcTemplate jdbcTemplate;
    private final int BATCH_SIZE = 100;

    /**
     * JoyStatusHistory 테이블에 batch insert
     * @param rows JoyStatusHistoryBatchRow 타입 리스트
     * @return insert된 레코드 개수
     */
    @Transactional
    public int batchInsert(List<JoyStatusHistoryBatchRow> rows) {
        int total = 0;
        for(int i = 0; i < rows.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, rows.size());
            List<JoyStatusHistoryBatchRow> chunk = rows.subList(i, end);

            int[] counts = jdbcTemplate.batchUpdate(
                "INSERT INTO joy_status_history (joy_order_id, to_status, reason_code, created_at) VALUES (?, ?, ?, ?);",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int idx) throws SQLException {
                        JoyStatusHistoryBatchRow row = chunk.get(idx);
                        ps.setLong(1, row.getJoyOrderId());
                        ps.setString(2, row.getToStatus().name());
                        ps.setString(3, row.getReasonCode());
                        ps.setTimestamp(4, Timestamp.valueOf(row.getCreatedAt()));
                    }

                    @Override
                    public int getBatchSize() {
                        return chunk.size();
                    }
                }
            );

            for(int c : counts) {
                if(c == PreparedStatement.SUCCESS_NO_INFO) {
                    total++;
                } else if(c == PreparedStatement.EXECUTE_FAILED) {
                    throw new IllegalStateException("JoyStatusHistory Batch Failed");
                } else {
                    total++;
                }
            }
        }
        return total;
    }

    @Getter
    public static class JoyStatusHistoryBatchRow {
        private final Long joyOrderId;
        private final JoyPaymentStatus toStatus;
        private final String reasonCode;
        private final LocalDateTime createdAt;
        JoyStatusHistoryBatchRow(Long joyOrderId, JoyPaymentStatus toStatus, String reasonCode) {
            this.joyOrderId = joyOrderId;
            this.toStatus = toStatus;
            this.reasonCode = reasonCode;
            this.createdAt = LocalDateTime.now();
        }
    }
}
