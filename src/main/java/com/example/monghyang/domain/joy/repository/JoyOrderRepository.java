package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.dto.ResJoyOrderDto;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JoyOrderRepository extends JpaRepository<JoyOrder, Long> {

    @Query("select jo from JoyOrder jo where jo.pgOrderId = :pgOrderId and jo.joyPaymentStatus = com.example.monghyang.domain.joy.entity.JoyPaymentStatus.PENDING")
    Optional<JoyOrder> findByPgOrderId(@Param("pgOrderId") UUID pgOrderId);

    @Query("select jo from JoyOrder jo join jo.joy.brewery b where jo.id = :joyOrderId and b.user.id = :userId")
    Optional<JoyOrder> findByIdAndBreweryUserId(@Param("joyOrderId") Long joyOrderId, @Param("userId") Long userId);

    @Query("select jo from JoyOrder jo where jo.id = :joyOrderId and jo.users.id = :userId")
    Optional<JoyOrder> findByIdAndUserId(@Param("joyOrderId") Long joyOrderId, @Param("userId") Long userId);

    @Query("""
    select new com.example.monghyang.domain.joy.dto.ResJoyOrderDto(jo.id, jo.users.id, j.id, j.name, jo.count, jo.totalAmount, jo.payerName, jo.payerPhone, jo.createdAt, jo.reservation, jo.joyPaymentStatus)
    from JoyOrder jo join jo.joy j on j.brewery.id = :breweryId and jo.joyPaymentStatus != com.example.monghyang.domain.joy.entity.JoyPaymentStatus.FAILED
    """)
    Page<ResJoyOrderDto> findByBreweryId(@Param("breweryId") Long breweryId, Pageable pageable);

    @Query("""
    select
        new com.example.monghyang.domain.joy.dto.ResJoyOrderDto(
        jo.id, jo.users.id, j.id, j.name, jo.count, jo.totalAmount, jo.payerName, jo.payerPhone, jo.createdAt,
        jo.reservation, jo.joyPaymentStatus)
    from JoyOrder jo join jo.joy j on j.brewery.id = :breweryId
    where date(jo.reservation) = :date
    and jo.joyPaymentStatus != com.example.monghyang.domain.joy.entity.JoyPaymentStatus.FAILED
    """)
    Page<ResJoyOrderDto> findByBreweryIdAndDate(@Param("breweryId") Long breweryId, Pageable pageable, @Param("date") LocalDate date);

    @Query("select new com.example.monghyang.domain.joy.dto.ResJoyOrderDto(jo.id, jo.users.id, j.id, j.name, jo.count, jo.totalAmount, jo.payerName, jo.payerPhone, jo.createdAt, jo.reservation, jo.joyPaymentStatus) from JoyOrder jo join jo.joy j on jo.users.id = :userId")
    Page<ResJoyOrderDto> findByUserId(Long userId, Pageable pageable);

    @Query("select jo from JoyOrder jo where jo.pgOrderId = :pgOrderId")
    Optional<JoyOrder> findByPgOrderIdForSetFailed(@Param("pgOrderId") UUID pgOrderId);

    /**
     * 지정된 '체험 예약 레코드'를 refund_requested 상태로 일괄 갱신
     * @param joyOrderIdList
     */
    @Modifying
    @Query("""
    update JoyOrder jo set jo.joyPaymentStatus = com.example.monghyang.domain.joy.entity.JoyPaymentStatus.REFUND_REQUESTED
    where jo.id in :joyOrderIdList
    """)
    void updatePaymentStatusToRefundRequestedByJoyIdListAndDate(@Param("joyOrderIdList") List<Long> joyOrderIdList);

    /**
     * 특정 체험들의 특정 날의 특정 상태인 '체험 예약'의 식별자 리스트 조회
     * @param joyIdList 체험 식별자 리스트
     * @param closedDate 특정 날(별도 휴무 지정 예정일)
     * @param status 특정 상태(JoyPaymentStatus)
     * @return '체험 예약' 식별자 리스트
     */
    @Query("""
    select jo.id from JoyOrder jo
    where jo.joy.id in :joyIdList
    and date(jo.reservation) = :closedDate
    and jo.joyPaymentStatus = :status
    """)
    List<Long> findIdByJoyIdListAndReservationAndStatus(@Param("joyIdList") List<Long> joyIdList, @Param("closedDate") LocalDate closedDate, @Param("status")JoyPaymentStatus status);
}
