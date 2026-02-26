package com.example.monghyang.domain.batch.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.global.pg.PayDBInfoDto;
import com.example.monghyang.domain.global.pg.Payment;
import com.example.monghyang.domain.global.pg.component.Cancel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JoyOrderRefundService {
    // 환불 프로세스 스케줄러 2: paymentKey에 해당하는 결제 정보를 '전액 환불' 처리. cancelReason은 'change brewery schedule' 로 통일
    public List<Payment> pgRefundProcess(List<PayDBInfoDto> refundRequestList) {
        int requestUnit = 10;
        List<Payment> refundResult = new ArrayList<>();
        try {
            for(int i = 0; i < refundRequestList.size(); i++) {
                if(i % requestUnit == 0) {
                    // 0.11초 대기하는 코드 작성(일단 tps 100 미만으로 설정: 초기값)
                    Thread.sleep(110);
                }
                // pg사로 환불요청: 추후 api 응답 결과로 매핑하기
                Payment payment = new Payment();
                makeMockPayment(payment, i);
                payment.setPayDBInfoDto(refundRequestList.get(i)); // payment 객체 필드에, 매핑되는 환불 레코드 핵심정보 추가

                refundResult.add(payment);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new ApplicationException(ApplicationError.SERVER_ERROR);
        }
        return refundResult;
    }
    /**
     * PG 연동 전, 테스트용 Payment 객체 필드 초기화
     * @param payment payment 객체
     * @param i 리스트 내부에서의 payment 객체 순서
     */
    private void makeMockPayment(Payment payment, int i) {
        List<Cancel> cancelList = new ArrayList<>();
        Cancel cancel = new Cancel();
        cancel.setTransactionKey("t"+i);
        if(i % 3 == 0) {
            cancel.setCancelStatus("ABORTED");
        } else {
            cancel.setCancelStatus("DONE");
        }
        cancelList.add(cancel);
        payment.setCancels(cancelList);
        payment.setLastTransactionKey(cancel.getTransactionKey());
    }
}
