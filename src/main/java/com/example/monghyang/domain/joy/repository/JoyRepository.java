package com.example.monghyang.domain.joy.repository;

import com.example.monghyang.domain.joy.entity.Joy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JoyRepository extends JpaRepository<Joy, Long> {

    /**
     * 체험과 소속 양조장이 모두 삭제되지 않은 경우만 식별자로 조회합니다.
     *
     * @param joyId 체험 식별자
     * @return 삭제되지 않은 체험
     */
    @Query("""
        select j from Joy j
        join j.brewery b
        where j.id = :joyId
          and j.isDeleted = false
          and b.isDeleted = false
    """)
    Optional<Joy> findActiveById(@Param("joyId") Long joyId);

    /**
     * 특정 양조장과 체험이 모두 삭제되지 않은 체험 목록을 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @return 삭제되지 않은 체험 목록
     */
    @Query("""
        select j from Joy j
        join j.brewery b
        where b.id = :breweryId
          and j.isDeleted = false
          and b.isDeleted = false
    """)
    List<Joy> findActiveByBreweryId(@Param("breweryId") Long breweryId);

    /**
     * 특정 회원이 관리하는 체험과 소속 양조장이 모두 삭제되지 않은 체험 목록을 조회합니다.
     *
     * @param userId 회원 식별자
     * @return 삭제되지 않은 체험 목록
     */
    @Query("""
        select j from Joy j
        join j.brewery b
        where b.user.id = :userId
          and j.isDeleted = false
          and b.isDeleted = false
    """)
    List<Joy> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 특정 양조장과 체험이 모두 삭제되지 않은 체험을 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @param joyId     체험 식별자
     * @return 삭제되지 않은 체험
     */
    @Query("""
        select j from Joy j
        join j.brewery b
        where j.id = :joyId
          and b.id = :breweryId
          and j.isDeleted = false
          and b.isDeleted = false
    """)
    Optional<Joy> findActiveByBreweryIdAndJoyId(@Param("breweryId") Long breweryId, @Param("joyId") Long joyId);

    /**
     * 양조장 관리자용 기능에서 소속 양조장의 탈퇴 여부와 무관하게 삭제되지 않은 체험을 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @param joyId     체험 식별자
     * @return 삭제되지 않은 체험
     */
    @Query("""
        select j from Joy j
        where j.id = :joyId
          and j.brewery.id = :breweryId
          and j.isDeleted = false
    """)
    Optional<Joy> findActiveByBreweryIdAndJoyIdIncludingDeletedBrewery(
            @Param("breweryId") Long breweryId,
            @Param("joyId") Long joyId
    );

    /**
     * 삭제되지 않은 양조장의 삭제된 체험을 복구 대상으로 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @param joyId     체험 식별자
     * @return 삭제된 체험
     */
    @Query("""
        select j from Joy j
        join j.brewery b
        where j.id = :joyId
          and b.id = :breweryId
          and j.isDeleted = true
          and b.isDeleted = false
    """)
    Optional<Joy> findDeletedByBreweryIdAndJoyId(@Param("breweryId") Long breweryId, @Param("joyId") Long joyId);

    /**
     * 예약 히스토리 정리를 위해 삭제 여부와 무관하게 체험 진행 시간을 조회합니다.
     *
     * @param joyId 체험 식별자
     * @return 체험 진행 시간 단위
     */
    @Query("select j.timeUnit from Joy j where j.id = :joyId")
    Optional<Integer> findTimeUnitByJoyId(@Param("joyId") Long joyId);

    /**
     * 양조장 일정 변경 환불 대상 선정을 위해 삭제 여부와 무관하게 체험 식별자를 조회합니다.
     *
     * @param breweryId 양조장 식별자
     * @return 체험 식별자 목록
     */
    @Query("select j.id from Joy j where j.brewery.id = :breweryId")
    List<Long> findIdByBreweryId(@Param("breweryId") Long breweryId);
}
