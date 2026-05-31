package com.example.monghyang.domain.batch.service;

import com.example.monghyang.domain.batch.dto.JoyStatusHistoryBatchRow;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.global.pg.PayDBInfoDto;
import com.example.monghyang.domain.global.pg.Payment;
import com.example.monghyang.domain.global.pg.component.Cancel;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JoyOrderBatchService {
    private final JdbcTemplate jdbcTemplate;
    private final JoyOrderRepository joyOrderRepository;
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

    // 환불 프로세스 스케줄러 1: 'refund_requested' 레코드 100개의 paymentKey 조회, 상태를 'refund_processing'으로 갱신 후 paymentKey return
    @Transactional
    public List<PayDBInfoDto> getPayInfoListAndUpdateStatusToRefundProcessing() {
        int batchSize = 100;
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt"); // 오래된 순으로 처리
        Pageable pageable = PageRequest.of(0, batchSize, sort);
        // 'refund_requested' 상태인 체험 예약 레코드 100개 select for update
        List<JoyOrder> joyOrderList = joyOrderRepository.findPaymentKeyByJoyPaymentStatusAndPageableForUpdate(pageable, JoyPaymentStatus.REFUND_REQUESTED);
        for(JoyOrder joyOrder : joyOrderList) {
            joyOrder.setRefundProcessing(); // refund_processing 으로 갱신
        }
        // 상태 변경 로깅 insert
        List<JoyStatusHistoryBatchRow> joyOrderRowList = new ArrayList<>();
        for(JoyOrder jo : joyOrderList) {
            joyOrderRowList.add(new JoyStatusHistoryBatchRow(
                    jo.getId(),
                    JoyPaymentStatus.REFUND_PROCESSING,
                    "양조장 일정 변경"
            ));
        }
        this.batchInsert(joyOrderRowList);
        return joyOrderList.stream().map(jo -> PayDBInfoDto.pkPaymentKeyOrderIdOf(jo.getId(), jo.getPgPaymentKey(), jo.getPgOrderId())).toList();
    }

    @Transactional
    public void refundResultProcess(List<Payment> refundResult) {
        List<Long> refundSuccessJoyOrderIdList = new ArrayList<>();
        List<Long> refundFailedJoyOrderIdList = new ArrayList<>();
        // 환불 결과로 응답받은 Payment 객체마다 결과에 따라 다르게 처리
        for(Payment payment : refundResult) {
            List<Cancel> cancelList = payment.getCancels();
            if(cancelList.isEmpty()) {
                log.info("체험 환불 결과를 찾을 수 없습니다. paymentKey: {}", payment.getPaymentKey());
            } else {
                Optional<Cancel> cancel = cancelList.stream()
                        .filter(c -> c.getTransactionKey().equals(payment.getLastTransactionKey()))
                        .findFirst();
                if(cancel.isPresent()) {
                    if(cancel.get().getCancelStatus().equals("DONE")) {
                        refundSuccessJoyOrderIdList.add(payment.getPayDBInfoDto().getPk());
                    } else {
                        refundFailedJoyOrderIdList.add(payment.getPayDBInfoDto().getPk());
                    }
                } else {
                    log.info("체험 환불 결과를 찾을 수 없습니다. paymentKey: {}", payment.getPaymentKey());
                    refundFailedJoyOrderIdList.add(payment.getPayDBInfoDto().getPk());
                }
            }
        }
        // 환불 성공/실패 레코드의 상태 갱신
        joyOrderRepository.updatePaymentStatusByJoyIdListAndStatus(refundSuccessJoyOrderIdList, JoyPaymentStatus.REFUNDED);
        joyOrderRepository.updatePaymentStatusByJoyIdListAndStatus(refundFailedJoyOrderIdList, JoyPaymentStatus.REFUND_FAILED);

        // joyStatusHistory 테이블에 상태 갱신 내역 로그 처리
        // 1. insert할 로그 레코드 데이터 생성
        List<JoyStatusHistoryBatchRow> logRowList = new ArrayList<>();

        for(Long joyOrderId : refundSuccessJoyOrderIdList) {
            logRowList.add(new JoyStatusHistoryBatchRow(
                    joyOrderId,
                    JoyPaymentStatus.REFUNDED,
                    "양조장 일정 변경"
            ));
        }
        for(Long joyOrderId : refundFailedJoyOrderIdList) {
            logRowList.add(new JoyStatusHistoryBatchRow(
                    joyOrderId,
                    JoyPaymentStatus.REFUND_FAILED,
                    "양조장 일정 변경"
            ));
        }
        // 2. batch insert
        this.batchInsert(logRowList);
    }
}
