package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.brewery.dto.JoyInfoDto;
import com.example.monghyang.domain.brewery.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.product.dto.ResProductOwnerDto;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.monghyang.domain.global.DayOfWeek;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BreweryRepository extends JpaRepository<Brewery, Long> {
    @Query("select b from Brewery b where b.user.id = :userId")
    Optional<Brewery> findByUserId(@Param("userId") Long userId);

    @Query("select b from Brewery b join fetch b.regionType join fetch b.user where b.id = :breweryId and b.isDeleted = false")
    Optional<Brewery> findActiveById(@Param("breweryId") Long breweryId);

    /**
     * 메소드 호출 시 모든 파라메터는 반드시 전달되어야 합니다. 적용되지 않는 필터링 요소에 대해서는 null 값을 대입하여 넘겨주세요.
     * @param pageable 페이징 크기 및 기준을 의미하는 인스턴스
     * @param tagListIsEmpty 태그 기준 필터링이 적용되었는지 여부를 나타내는 플래그 변수
     * @param regionListIsEmpty 지역 기준 필터링이 적용되었는지 여부를 나타내는 플래그 변수
     * @param keyword 양조장 이름 키워드 필터링 조건
     * @param minPrice 최소 체험 가격 필터링 조건
     * @param maxPrice 최대 체험 가격 필터링 조건
     * @param tagIdList 태그 식별자 리스트 필터링 조건
     * @param regionIdList 지역 식별자 리스트 필터링 조건
     */
    @Query("""
        select distinct new com.example.monghyang.domain.brewery.dto.ResBreweryListDto(b.id, b.breweryName, r.name, b.introduction, b.minJoyPrice, b.joyCount, bi.imageKey, b.isVisitingBrewery, b.isRegularVisit)
        from Brewery b
        join b.regionType r
        left join BreweryImage bi on b.id = bi.brewery.id and bi.seq = 1
        where
          (:tagListIsEmpty = true or exists (
             select 1
             from BreweryTag bt where bt.brewery = b and bt.tags.id in (:tagIdList)
          ))
          and (:keyword is null or b.breweryName like concat('%', :keyword, '%'))
          and (:regionListIdEmpty = true or r.id in (:regionIdList))
          and (
               :minPrice is null or :maxPrice is null
               or exists (
                   select 1 from Joy j
                   where j.brewery = b
                     and j.isDeleted = false
                     and (:minPrice is null or j.finalPrice >= :minPrice)
                     and (:maxPrice is null or j.finalPrice <= :maxPrice)
               )
          )
          and b.isDeleted = false
    """)
    Page<ResBreweryListDto> findByDynamicFiltering(Pageable pageable, @Param("tagListIsEmpty") boolean tagListIsEmpty, @Param("regionListIdEmpty") boolean regionListIsEmpty,
                                                            @Param("keyword") String keyword, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice,
                                                            @Param("tagIdList") List<Integer> tagIdList, @Param("regionIdList") List<Integer> regionIdList);

    @Query("""
        select distinct new com.example.monghyang.domain.brewery.dto.ResBreweryListDto(b.id, b.breweryName, r.name, b.introduction, b.minJoyPrice, b.joyCount, bi.imageKey, b.isVisitingBrewery, b.isRegularVisit)
        from Brewery b
        join b.regionType r
        left join BreweryImage bi on b.id = bi.brewery.id and bi.seq = 1
        where b.isDeleted = false
    """)
    Page<ResBreweryListDto> findBreweryLatest(Pageable pageable);

    @Query("select new com.example.monghyang.domain.product.dto.ResProductOwnerDto(b.id, r.name, bi.imageKey) from Brewery b left join b.regionType r left join BreweryImage bi on bi.brewery = b and bi.seq = 1 where b.user.id = :userId")
    Optional<ResProductOwnerDto> findSimpleInfoByUserId(@Param("userId") Long userId);

    /**
     * 예약 일자와 요일에 해당하는 양조장 운영시간 스냅샷을 조회합니다.
     * <p>
     * effective_date &lt;= reservationDate 조건 중 MAX(effective_date) 스냅샷을 선택하고,
     * 해당 스냅샷 내에서 dayOfWeek가 일치하는 레코드의 openTime/closeTime을 반환합니다.
     * 조건에 맞는 스냅샷이 없으면 Optional.empty()를 반환합니다.
     *
     * @param joyId           체험 식별자
     * @param reservationDate 예약 일자
     * @param dayOfWeek       예약 일자의 요일
     * @return 운영시간, 체험 단위, 최대/최소 인원 정보
     */
    @Query("""
        select new com.example.monghyang.domain.brewery.dto.JoyInfoDto(
            wot.openTime, wot.closeTime, j.timeUnit, j.maxCount, j.minCount)
        from Joy j
        join j.brewery b
        join BreweryWeeklyOpenTime wot on wot.brewery = b
        where j.id = :joyId
          and wot.dayOfWeek = :dayOfWeek
          and wot.effectiveDate = (
              select max(wot2.effectiveDate)
              from BreweryWeeklyOpenTime wot2
              where wot2.brewery = b
                and wot2.dayOfWeek = :dayOfWeek
                and wot2.effectiveDate <= :reservationDate
          )
        """)
    Optional<JoyInfoDto> findJoyTimeInfoByJoyId(
            @Param("joyId") Long joyId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
}
