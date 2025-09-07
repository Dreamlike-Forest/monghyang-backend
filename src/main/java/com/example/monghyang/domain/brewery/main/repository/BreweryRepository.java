package com.example.monghyang.domain.brewery.main.repository;

import com.example.monghyang.domain.brewery.main.dto.ResBreweryListDto;
import com.example.monghyang.domain.brewery.main.entity.Brewery;
import com.example.monghyang.domain.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BreweryRepository extends JpaRepository<Brewery, Long> {
    @Query("select b.user.phone from Brewery b where b.id = :breweryId")
    Optional<String> findPhoneById(@Param("breweryId") Long breweryId);

    @Query("select b from Brewery b where b.user.id = :userId")
    Optional<Brewery> findByUserId(@Param("userId") Long userId);

    @Query("select b from Brewery b join fetch b.regionType join fetch b.user where b.id = :breweryId and b.isDeleted = false")
    Optional<Brewery> findActiveById(@Param("breweryId") Long breweryId);

    /**
     * 가격 기준 필터링 조회 시 '최소 가격'과 '최대 가격' 모두 유효한 값이 전달되어야 합니다.
     * 모든 파라메터는 반드시 전달되어야 합니다. 적용되지 않는 필터링 요소에 대해서는 null 값을 대입하여 넘겨주세요.
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
        select distinct new com.example.monghyang.domain.brewery.main.dto.ResBreweryListDto(b.id, b.breweryName, r.name, b.introduction, b.minJoyPrice, b.joyCount, bi.imageKey, b.isVisitingBrewery, b.isRegularVisit)
        from Brewery b
        join b.regionType r
        left join BreweryImage bi on b.id = bi.brewery.id and bi.seq = 1
        where
          (:tagListIsEmpty = true or exists (
             select 1
             from BreweryTag bt2 join bt2.tags t2
             where bt2.brewery = b and t2.id in (:tagIdList)
          ))
          and (:keyword is null or b.breweryName like concat('%', :keyword, '%'))
          and (:regionListIdEmpty = true or r.id in (:regionIdList))
          and (
               :minPrice is null or :maxPrice is null
               or exists (
                   select 1 from Joy j
                   where j.brewery = b
                     and j.isDeleted = false
                     and j.finalPrice between :minPrice and :maxPrice
               )
          )
          and b.isDeleted = false
    """)
    Page<ResBreweryListDto> findByDynamicFiltering(Pageable pageable, @Param("tagListIsEmpty") boolean tagListIsEmpty, @Param("regionListIdEmpty") boolean regionListIsEmpty,
                                                            @Param("keyword") String keyword, @Param("minPrice") Integer minPrice, @Param("maxPrice") Integer maxPrice,
                                                            @Param("tagIdList") List<Integer> tagIdList, @Param("regionIdList") List<Integer> regionIdList);

    @Query("""
        select distinct new com.example.monghyang.domain.brewery.main.dto.ResBreweryListDto(b.id, b.breweryName, r.name, b.introduction, b.minJoyPrice, b.joyCount, bi.imageKey, b.isVisitingBrewery, b.isRegularVisit)
        from Brewery b
        join b.regionType r
        left join BreweryImage bi on b.id = bi.brewery.id and bi.seq = 1
        where b.isDeleted = false
    """)
    Page<ResBreweryListDto> findBreweryLatest(Pageable pageable);
}
