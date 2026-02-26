package com.example.monghyang.domain.global.pg;

import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PayDBInfoDto {
    private final Long pk;
    private final String pgPaymentKey;
    private final BigDecimal amount;
    private final UUID pgOrderId;
    private PayDBInfoDto(Long pk, String pgPaymentKey, UUID pgOrderId, BigDecimal amount) {
        this.pk = pk;
        this.pgPaymentKey = pgPaymentKey;
        this.amount = amount;
        this.pgOrderId = pgOrderId;
    }

    /**
     * (부분 환불) PG사 API 요청을 위한 DB의 결제 내역 정보 DTO 생성자
     * @param pk 해당 레코드의 식별자
     * @param pgPaymentKey paymentKey
     * @param pgOrderId 본 서버에서 발급했던 orderId
     * @param amount 환불 금액
     * @return
     */
    public static PayDBInfoDto pkPaymentKeyOrderIdAmountOf(@NonNull Long pk, @NonNull String pgPaymentKey, @NonNull UUID pgOrderId, BigDecimal amount) {
        return new PayDBInfoDto(pk, pgPaymentKey, pgOrderId, amount);
    }

    /**
     * (전액 환불) PG사 API 요청을 위한 DB의 결제 내역 정보 DTO 생성자
     * @param pk 해당 레코드의 식별자
     * @param pgPaymentKey paymentKey
     * @param pgOrderId 본 서버에서 발급했던 orderId
     * @return
     */
    public static PayDBInfoDto pkPaymentKeyOrderIdOf(@NonNull Long pk, @NonNull String pgPaymentKey, @NonNull UUID pgOrderId) {
        return new PayDBInfoDto(pk, pgPaymentKey, pgOrderId, null);
    }
}
