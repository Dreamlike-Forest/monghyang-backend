package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.dto.ResJoyOrderDto;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * PK를 조건으로, '체험 예약 레코드'를 특정 JoyPaymentStatus 상태로 일괄 갱신
     * @param joyOrderIdList 체험 예약 레코드 식별자 리스트
     * @param status JoyPaymentStatus
     */
    @Modifying
    @Query("""
    update JoyOrder jo set jo.joyPaymentStatus = :status
    where jo.id in :joyOrderIdList
    """)
    void updatePaymentStatusByJoyIdListAndStatus(@Param("joyOrderIdList") List<Long> joyOrderIdList, @Param("status") JoyPaymentStatus status);

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

    /**
     * 특정 status 인 레코드 N개 select for update
     * @param pageable 조회할 레코드 수(0, N)
     * @param status 조회 조건
     * @return N개의 레코드
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select jo from JoyOrder jo where jo.joyPaymentStatus = :status")
    List<JoyOrder> findPaymentKeyByJoyPaymentStatusAndPageableForUpdate(Pageable pageable, @Param("status") JoyPaymentStatus status);

    /**
     * 특정 체험 목록의 effective_date 포함 이후 날짜에 예약된 특정 상태의 체험 예약 식별자 목록을 조회합니다.
     * 양조장 스케줄 변경 시 환불 처리 대상 예약을 선정하는 데 사용합니다.
     *
     * @param joyIdList     체험 식별자 리스트
     * @param effectiveDate 스케줄 적용 시작일 (이 날짜 포함 이후)
     * @param status        조회 대상 결제 상태
     * @return 조건에 해당하는 체험 예약 식별자 리스트
     */
    @Query("""
    select jo.id from JoyOrder jo
    where jo.joy.id in :joyIdList
    and date(jo.reservation) >= :effectiveDate
    and jo.joyPaymentStatus = :status
    """)
    List<Long> findIdByJoyIdListAndReservationOnOrAfterAndStatus(
            @Param("joyIdList") List<Long> joyIdList,
            @Param("effectiveDate") LocalDate effectiveDate,
            @Param("status") JoyPaymentStatus status
    );

    /**
     * 특정 체험의 적용일 시작 시각 이후 환불 요청 대상 예약 식별자를 조회합니다.
     *
     * @param joyId               체험 식별자
     * @param reservationFrom     환불 대상 예약 시작 시각
     * @param joyPaymentStatus    환불 대상 결제 상태
     * @param isDeleted           삭제 여부
     * @return 환불 요청 대상 체험 예약 식별자 목록
     */
    @Query("""
    select jo.id from JoyOrder jo
    where jo.joy.id = :joyId
    and jo.reservation >= :reservationFrom
    and jo.joyPaymentStatus = :joyPaymentStatus
    and jo.isDeleted = :isDeleted
    """)
    List<Long> findIdByJoyIdAndReservationFromAndPaymentStatusAndIsDeleted(
            @Param("joyId") Long joyId,
            @Param("reservationFrom") LocalDateTime reservationFrom,
            @Param("joyPaymentStatus") JoyPaymentStatus joyPaymentStatus,
            @Param("isDeleted") Boolean isDeleted
    );

    /**
     * 양조장의 스케줄 적용일 이후 PAID 예약 후보를 조회합니다.
     *
     * @param breweryId        양조장 식별자
     * @param reservationFrom  적용일 시작 시각
     * @param joyPaymentStatus 결제 상태
     * @param isDeleted        삭제 여부
     * @return 휴게시간 영향 여부를 서비스에서 판정할 예약 후보 목록
     */
    @Query("""
    select jo from JoyOrder jo
    join fetch jo.joy j
    where j.brewery.id = :breweryId
    and jo.reservation >= :reservationFrom
    and jo.joyPaymentStatus = :joyPaymentStatus
    and jo.isDeleted = :isDeleted
    """)
    List<JoyOrder> findByBreweryIdAndReservationFromAndPaymentStatusAndIsDeleted(
            @Param("breweryId") Long breweryId,
            @Param("reservationFrom") LocalDateTime reservationFrom,
            @Param("joyPaymentStatus") JoyPaymentStatus joyPaymentStatus,
            @Param("isDeleted") Boolean isDeleted
    );

    /**
     * 체험 삭제 시 삭제 시점 이후 PAID 예약을 잠금 조회합니다.
     *
     * @param joyId            체험 식별자
     * @param reservationFrom  삭제 시점
     * @param joyPaymentStatus 환불 대상 결제 상태
     * @param isDeleted        예약 내역 삭제 여부
     * @return 환불 요청 대상 체험 예약 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select jo from JoyOrder jo
    where jo.joy.id = :joyId
    and jo.reservation >= :reservationFrom
    and jo.joyPaymentStatus = :joyPaymentStatus
    and jo.isDeleted = :isDeleted
    """)
    List<JoyOrder> findByJoyIdAndReservationFromAndPaymentStatusAndIsDeletedForUpdate(
            @Param("joyId") Long joyId,
            @Param("reservationFrom") LocalDateTime reservationFrom,
            @Param("joyPaymentStatus") JoyPaymentStatus joyPaymentStatus,
            @Param("isDeleted") Boolean isDeleted
    );

    /**
     * 양조장 삭제 시 삭제 시점 이후 PAID 예약을 잠금 조회합니다.
     *
     * @param breweryId        양조장 식별자
     * @param reservationFrom  삭제 시점
     * @param joyPaymentStatus 환불 대상 결제 상태
     * @param isDeleted        예약 내역 삭제 여부
     * @return 환불 요청 대상 체험 예약 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select jo from JoyOrder jo
    join jo.joy j
    where j.brewery.id = :breweryId
    and jo.reservation >= :reservationFrom
    and jo.joyPaymentStatus = :joyPaymentStatus
    and jo.isDeleted = :isDeleted
    """)
    List<JoyOrder> findByBreweryIdAndReservationFromAndPaymentStatusAndIsDeletedForUpdate(
            @Param("breweryId") Long breweryId,
            @Param("reservationFrom") LocalDateTime reservationFrom,
            @Param("joyPaymentStatus") JoyPaymentStatus joyPaymentStatus,
            @Param("isDeleted") Boolean isDeleted
    );
}
