# 주간 스냅샷 버전 기준 조회 구현 계획

> **Agentic worker 필수 지침:** 이 계획을 구현할 때는 `superpowers:executing-plans`를 사용한다. 구현 전에는 `superpowers:using-git-worktrees`를 적용하고, 각 단계는 체크박스(`- [ ]`)로 추적한다.

**목표:** 양조장 운영시간, 양조장 휴게시간, 체험 시작시간 조회가 요일별 과거 최신값을 되살리지 않고 `effective_date` 하나를 전체 주간 스냅샷 버전으로 해석하도록 수정한다.

**아키텍처:** 저장 모델은 그대로 둔다. 새 `effective_date`에 저장된 row 집합을 전체 주간표로 보고, 그 버전에 없는 운영요일, 휴게시간, 체험 시작시간은 “없음”으로 해석한다. `None` 요일, sentinel time, 신규 테이블, DB 마이그레이션은 도입하지 않는다.

**기술 스택:** Java 21, Spring Boot 3.5.3, Spring Data JPA 3.5.1, Jakarta Validation 3.0.2, JUnit 5.12.2, Mockito 5.17.0/5.14.2.

**의존성 기준:** `docs/context7-dependencies.yaml`

**Context7:** `context7_library_id: not_used` - 정확한 외부 API signature나 버전별 설정값 확인이 아니라 기존 JPQL과 서비스 로직의 도메인 의미를 고치는 작업이므로 호출하지 않는다.

---

## 범위와 전제

- `/api/brewery-priv/schedule` 요청의 `schedules`는 새 `effective_date`부터 적용되는 전체 양조장 주간 운영표이다.
- 양조장 요청에 포함되지 않은 요일은 새 주간 버전에서 미운영으로 해석한다.
- 양조장 요청에 포함된 요일에서 `break_start`, `break_end`가 둘 다 없으면 해당 요일은 운영하지만 휴게시간은 없는 것으로 해석한다.
- `/api/brewery-priv/joy/schedule` 요청의 `schedules`는 새 `effective_date`부터 적용되는 전체 체험 주간 시작시간표이다.
- 체험 요청에 포함되지 않은 요일은 새 주간 버전에서 체험 미운영으로 해석한다.
- `ReqUpdateJoyScheduleDto.schedules`의 빈 목록은 이번 작업에서 허용하지 않는다. 전체 체험 일정 없음까지 지원하려면 row가 0개인 버전을 저장할 스냅샷 헤더 테이블이 필요하므로 별도 설계가 필요하다.
- DB 스키마 변경은 하지 않는다.
- 코드 변경 커밋은 사용자 확인 전에는 만들지 않는다. 계획 문서 커밋은 저장소 규칙상 허용된다.

## 파일 구조

- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
  - 월 단위/일 단위 슬롯 조회에서 최신 주간 스냅샷 버전 기준으로 운영시간, 휴게시간, 체험 시작시간을 선택한다.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryRepository.java`
  - 예약 생성/변경 검증용 운영시간 조회가 요일별 최신값이 아니라 최신 주간 버전 안의 요일 row를 찾도록 한다.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepository.java`
  - 예약 검증과 환불 판단용 휴게시간 조회가 최신 양조장 운영시간 주간 버전의 휴게시간만 반환하도록 한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepository.java`
  - 예약 검증용 체험 시작시간 조회가 최신 체험 주간 버전 안의 요일 row를 찾도록 한다.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`
  - 휴게 시작/종료 중 하나만 입력된 요청을 거부한다.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/dto/ReqUpdateBreweryScheduleDto.java`
  - 전체 주간 스냅샷 교체 의미를 DTO 주석에 명시한다.
- 수정: `src/main/java/com/example/monghyang/domain/auth/dto/BreweryScheduleDto.java`
  - 휴게시간 필드의 의미와 null 조합 계약을 주석으로 명시한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyScheduleDto.java`
  - 누락 요일이 체험 미운영임을 DTO 주석에 명시한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/dto/JoyScheduleDto.java`
  - `start_times`는 운영 요일에만 필요하고 미운영 요일은 항목 생략으로 표현함을 주석에 명시한다.
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
  - 운영요일 제거, 휴게시간 제거, 체험 운영요일 제거가 과거 row로 되살아나지 않는지 검증한다.
- 수정: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`
  - 휴게시간 제거 저장 방식과 휴게 시작/종료 쌍 검증을 고정한다.
- 검증: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
  - 체험 일정 변경 저장 흐름이 기존대로 통과하는지 확인한다.
- 검증: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
  - 예약 검증과 환불 판단 흐름이 변경된 repository 계약과 충돌하지 않는지 확인한다.

---

### Task 1: 실패하는 슬롯 조회 테스트 추가

**파일:**
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`

- [ ] **Step 1: 테스트 헬퍼가 요일과 적용일을 받을 수 있게 확장한다**

현재 헬퍼는 `DayOfWeek.Mon`, `2026-06-01`로 고정되어 있다. 기존 헬퍼를 아래 형태로 교체하고, 기존 테스트 호출부는 같은 의미가 유지되도록 오버로드를 둔다.

```java
private BreweryWeeklyOpenTime openTime(LocalTime openTime, LocalTime closeTime) {
    return openTime(DayOfWeek.Mon, LocalDate.of(2026, 6, 1), openTime, closeTime);
}

private BreweryWeeklyOpenTime openTime(DayOfWeek dayOfWeek, LocalDate effectiveDate, LocalTime openTime, LocalTime closeTime) {
    return BreweryWeeklyOpenTime.builder()
            .brewery(mock(Brewery.class))
            .dayOfWeek(dayOfWeek)
            .openTime(openTime)
            .closeTime(closeTime)
            .effectiveDate(effectiveDate)
            .build();
}

private JoyWeeklyStartTime startTime(LocalTime startTime) {
    return startTime(DayOfWeek.Mon, LocalDate.of(2026, 6, 1), startTime);
}

private JoyWeeklyStartTime startTime(DayOfWeek dayOfWeek, LocalDate effectiveDate, LocalTime startTime) {
    return JoyWeeklyStartTime.joyDayOfWeekStartTimeEffectiveDateOf(
            mock(Joy.class),
            dayOfWeek,
            startTime,
            effectiveDate
    );
}

private BreweryWeeklyBreakTime breakTime(LocalTime breakStart, LocalTime breakEnd) {
    return breakTime(DayOfWeek.Mon, LocalDate.of(2026, 6, 1), breakStart, breakEnd);
}

private BreweryWeeklyBreakTime breakTime(DayOfWeek dayOfWeek, LocalDate effectiveDate, LocalTime breakStart, LocalTime breakEnd) {
    return BreweryWeeklyBreakTime.builder()
            .brewery(mock(Brewery.class))
            .dayOfWeek(dayOfWeek)
            .breakStart(breakStart)
            .breakEnd(breakEnd)
            .effectiveDate(effectiveDate)
            .build();
}
```

- [ ] **Step 2: 최신 양조장 주간 버전에 없는 운영요일은 과거 운영요일로 되살리지 않는 테스트를 추가한다**

`JoySlotServiceTest`에 아래 테스트를 추가한다. 현재 구현에서는 과거 월요일 운영시간을 선택해 `10:00`이 응답에 들어가므로 실패해야 한다.

```java
@Test
@DisplayName("최신 양조장 주간 버전에 없는 요일은 과거 운영요일로 되살리지 않는다")
void get_remaining_count_list_uses_latest_brewery_weekly_version_for_open_time() {
    Long joyId = 10L;
    Long breweryId = 20L;
    LocalDate targetDate = LocalDate.of(2026, 7, 6);
    Joy joy = joy(breweryId, 60);
    given(joy.getMaxCount()).willReturn(10);

    given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(joy));
    given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of(
                    openTime(DayOfWeek.Mon, LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    openTime(DayOfWeek.Tue, LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0))
            ));
    given(breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of());
    given(joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of(startTime(DayOfWeek.Mon, LocalDate.of(2026, 7, 1), LocalTime.of(10, 0))));
    given(joySlotRepository.findByJoyIdAndDate(joyId, targetDate)).willReturn(List.of());

    ResJoySlotTimeDto result = joySlotService.getRemainingCountList(joyId, targetDate);

    assertTrue(result.getTime_info().isEmpty());
    assertTrue(result.getRemaining_count_list().isEmpty());
}
```

- [ ] **Step 3: 최신 양조장 주간 버전에 없는 휴게시간은 과거 휴게시간으로 되살리지 않는 테스트를 추가한다**

현재 구현에서는 과거 휴게시간을 선택해 `12:00`이 제외되므로 실패해야 한다.

```java
@Test
@DisplayName("최신 양조장 주간 버전에 없는 휴게시간은 과거 휴게시간으로 되살리지 않는다")
void get_remaining_count_list_uses_latest_brewery_weekly_version_for_break_time() {
    Long joyId = 10L;
    Long breweryId = 20L;
    LocalDate targetDate = LocalDate.of(2026, 7, 6);
    Joy joy = joy(breweryId, 60);
    given(joy.getMaxCount()).willReturn(10);

    given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(joy));
    given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of(
                    openTime(DayOfWeek.Mon, LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0)),
                    openTime(DayOfWeek.Mon, LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0))
            ));
    given(breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of(breakTime(DayOfWeek.Mon, LocalDate.of(2026, 6, 1), LocalTime.of(12, 0), LocalTime.of(13, 0))));
    given(joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of(startTime(DayOfWeek.Mon, LocalDate.of(2026, 7, 1), LocalTime.of(12, 0))));
    given(joySlotRepository.findByJoyIdAndDate(joyId, targetDate)).willReturn(List.of());

    ResJoySlotTimeDto result = joySlotService.getRemainingCountList(joyId, targetDate);

    assertEquals(List.of(LocalTime.of(12, 0)), result.getTime_info());
}
```

- [ ] **Step 4: 최신 체험 주간 버전에 없는 요일은 과거 시작시간으로 되살리지 않는 테스트를 추가한다**

현재 구현에서는 과거 월요일 시작시간을 선택해 `10:00`이 응답에 들어가므로 실패해야 한다.

```java
@Test
@DisplayName("최신 체험 주간 버전에 없는 요일은 과거 시작시간으로 되살리지 않는다")
void get_remaining_count_list_uses_latest_joy_weekly_version_for_start_time() {
    Long joyId = 10L;
    Long breweryId = 20L;
    LocalDate targetDate = LocalDate.of(2026, 7, 6);
    Joy joy = joy(breweryId, 60);
    given(joy.getMaxCount()).willReturn(10);

    given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(joy));
    given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of(openTime(DayOfWeek.Mon, LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0))));
    given(breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of());
    given(joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, targetDate, targetDate.plusDays(1)))
            .willReturn(List.of(
                    startTime(DayOfWeek.Mon, LocalDate.of(2026, 6, 1), LocalTime.of(10, 0)),
                    startTime(DayOfWeek.Tue, LocalDate.of(2026, 7, 1), LocalTime.of(11, 0))
            ));
    given(joySlotRepository.findByJoyIdAndDate(joyId, targetDate)).willReturn(List.of());

    ResJoySlotTimeDto result = joySlotService.getRemainingCountList(joyId, targetDate);

    assertTrue(result.getTime_info().isEmpty());
    assertTrue(result.getRemaining_count_list().isEmpty());
}
```

- [ ] **Step 5: 실패 확인**

실행:

```bash
./gradlew test --tests '*JoySlotServiceTest'
```

기대 결과:

- Step 2 테스트는 `result.getTime_info().isEmpty()` assertion에서 실패한다.
- Step 3 테스트는 `List.of(12:00)` 기대값과 빈 목록이 달라 실패한다.
- Step 4 테스트는 `result.getTime_info().isEmpty()` assertion에서 실패한다.

---

### Task 2: `JoySlotService` 메모리 조회 로직 수정

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`

- [ ] **Step 1: 운영시간 선택 로직을 최신 주간 버전 기준으로 변경한다**

`findActiveOpenTime`을 아래처럼 교체한다. 요일 필터를 먼저 적용하지 않고, 예약일 이하의 최신 `effectiveDate`를 먼저 고른 뒤 해당 버전 안에서 요일 row를 찾는다.

```java
private BreweryWeeklyOpenTime findActiveOpenTime(List<BreweryWeeklyOpenTime> wotList, LocalDate date, DayOfWeek dayOfWeek) {
    LocalDate latestEffectiveDate = wotList.stream()
            .filter(w -> !w.getEffectiveDate().isAfter(date))
            .map(BreweryWeeklyOpenTime::getEffectiveDate)
            .max(LocalDate::compareTo)
            .orElse(null);
    if (latestEffectiveDate == null) {
        return null;
    }
    return wotList.stream()
            .filter(w -> w.getEffectiveDate().equals(latestEffectiveDate) && w.getDayOfWeek() == dayOfWeek)
            .findFirst()
            .orElse(null);
}
```

- [ ] **Step 2: 체험 시작시간 선택 로직을 최신 주간 버전 기준으로 변경한다**

`findActiveStartTimes`를 아래처럼 교체한다.

```java
private List<JoyWeeklyStartTime> findActiveStartTimes(List<JoyWeeklyStartTime> jwstList, LocalDate date, DayOfWeek dayOfWeek) {
    LocalDate latestEffectiveDate = jwstList.stream()
            .filter(jw -> !jw.getEffectiveDate().isAfter(date))
            .map(JoyWeeklyStartTime::getEffectiveDate)
            .max(LocalDate::compareTo)
            .orElse(null);
    if (latestEffectiveDate == null) {
        return List.of();
    }
    return jwstList.stream()
            .filter(jw -> jw.getEffectiveDate().equals(latestEffectiveDate) && jw.getDayOfWeek() == dayOfWeek)
            .toList();
}
```

- [ ] **Step 3: 휴게시간 선택 로직을 양조장 최신 주간 버전 기준으로 변경한다**

`findActiveBreakTimes`의 signature와 구현을 아래처럼 변경한다. 휴게시간 자체의 최신 버전을 찾지 않고, 이미 선택된 운영시간 주간 버전의 `effectiveDate`를 기준으로만 찾는다.

```java
private List<BreweryWeeklyBreakTime> findActiveBreakTimes(
        List<BreweryWeeklyBreakTime> breakTimeList,
        DayOfWeek dayOfWeek,
        LocalDate weeklyEffectiveDate
) {
    return breakTimeList.stream()
            .filter(b -> b.getEffectiveDate().equals(weeklyEffectiveDate) && b.getDayOfWeek() == dayOfWeek)
            .toList();
}
```

- [ ] **Step 4: 월 예약 불가 날짜 조회 호출부를 변경한다**

`getImpossibleDate` 안에서 `activeBreakTimes` 계산부를 아래처럼 바꾼다.

```java
List<BreweryWeeklyBreakTime> activeBreakTimes = findActiveBreakTimes(
        breakTimeList,
        dayOfWeek,
        openTimeInfo.getEffectiveDate()
);
```

- [ ] **Step 5: 일별 남은 자리 조회 호출부를 변경한다**

`getRemainingCountList` 안에서 `activeBreakTimes` 계산부를 `openTimeInfo` null 확인 뒤로 이동한다.

```java
if (openTimeInfo != null && openTimeInfo.getOpenTime() != null && openTimeInfo.getCloseTime() != null) {
    LocalTime openTime = openTimeInfo.getOpenTime();
    LocalTime closeTime = openTimeInfo.getCloseTime();
    List<BreweryWeeklyBreakTime> activeBreakTimes = findActiveBreakTimes(
            breakTimeList,
            dayOfWeek,
            openTimeInfo.getEffectiveDate()
    );

    List<LocalTime> activeStartTimes = startTimes.stream()
            .map(JoyWeeklyStartTime::getStartTime)
            .filter(t -> !t.isBefore(openTime) && !t.plusMinutes(joy.getTimeUnit()).isAfter(closeTime))
            .filter(t -> !overlapsBreakTime(t, joy.getTimeUnit(), activeBreakTimes))
            .sorted()
            .toList();

    result.getTime_info().addAll(activeStartTimes);
}
```

- [ ] **Step 6: 테스트 통과 확인**

실행:

```bash
./gradlew test --tests '*JoySlotServiceTest'
```

기대 결과:

```text
BUILD SUCCESSFUL
```

---

### Task 3: Repository 직접 조회를 최신 주간 버전 기준으로 변경

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryRepository.java`
- 수정: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepository.java`
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepository.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`

- [ ] **Step 1: `BreweryRepository.findJoyTimeInfoByJoyId`의 운영시간 subquery를 수정한다**

기존 subquery의 `wot2.dayOfWeek = :dayOfWeek` 조건을 제거한다. outer query는 계속 `wot.dayOfWeek = :dayOfWeek`로 필터링한다.

```java
@Query("""
    select new com.example.monghyang.domain.brewery.dto.JoyInfoDto(
        b.id, wot.openTime, wot.closeTime, j.timeUnit, j.maxCount, j.minCount)
    from Joy j
    join j.brewery b
    join BreweryWeeklyOpenTime wot on wot.brewery = b
    where j.id = :joyId
      and j.isDeleted = false
      and b.isDeleted = false
      and wot.dayOfWeek = :dayOfWeek
      and wot.effectiveDate = (
          select max(wot2.effectiveDate)
          from BreweryWeeklyOpenTime wot2
          where wot2.brewery = b
            and wot2.effectiveDate <= :reservationDate
      )
    """)
Optional<JoyInfoDto> findJoyTimeInfoByJoyId(
        @Param("joyId") Long joyId,
        @Param("reservationDate") LocalDate reservationDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
);
```

- [ ] **Step 2: `BreweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate`의 기준 버전을 운영시간 주간 버전으로 변경한다**

휴게시간 row가 없는 버전은 휴게시간 없음이어야 하므로, subquery는 `BreweryWeeklyBreakTime`이 아니라 `BreweryWeeklyOpenTime`의 최신 주간 버전을 기준으로 삼는다.

```java
@Query("""
    select bbt from BreweryWeeklyBreakTime bbt
    where bbt.brewery.id = :breweryId
      and bbt.dayOfWeek = :dayOfWeek
      and bbt.effectiveDate = (
          select max(wot.effectiveDate)
          from BreweryWeeklyOpenTime wot
          where wot.brewery.id = :breweryId
            and wot.effectiveDate <= :targetDate
      )
""")
List<BreweryWeeklyBreakTime> findActiveBreakTimesByBreweryIdAndDate(
        @Param("breweryId") Long breweryId,
        @Param("targetDate") LocalDate targetDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
);
```

- [ ] **Step 3: `JoyWeeklyStartTimeRepository.findActiveStartTimesByJoyIdAndDate`의 시작시간 subquery를 수정한다**

기존 subquery의 `jwst2.dayOfWeek = :dayOfWeek` 조건을 제거한다. outer query는 계속 `jwst.dayOfWeek = :dayOfWeek`로 필터링한다.

```java
@Query("""
    select jwst from JoyWeeklyStartTime jwst
    where jwst.joy.id = :joyId
      and jwst.dayOfWeek = :dayOfWeek
      and jwst.effectiveDate = (
          select max(jwst2.effectiveDate)
          from JoyWeeklyStartTime jwst2
          where jwst2.joy.id = :joyId
            and jwst2.effectiveDate <= :targetDate
      )
""")
List<JoyWeeklyStartTime> findActiveStartTimesByJoyIdAndDate(
        @Param("joyId") Long joyId,
        @Param("targetDate") LocalDate targetDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
);
```

- [ ] **Step 4: Repository Javadocs를 최신 주간 버전 기준으로 갱신한다**

세 repository method의 Javadocs에 아래 의미를 반영한다.

```java
/**
 * 예약일 기준 최신 주간 스냅샷 버전을 먼저 선택한 뒤, 그 버전 안에서 특정 요일의 row를 조회합니다.
 * 최신 버전에 해당 요일 row가 없으면 과거 row를 되살리지 않고 빈 결과를 반환합니다.
 */
```

- [ ] **Step 5: 기존 서비스 테스트가 깨지지 않는지 확인한다**

실행:

```bash
./gradlew test --tests '*JoyServiceTest' --tests '*JoyOrderServiceTest'
```

기대 결과:

```text
BUILD SUCCESSFUL
```

---

### Task 4: 양조장 일정 변경 요청 의미와 휴게시간 검증 고정

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`
- 수정: `src/main/java/com/example/monghyang/domain/brewery/dto/ReqUpdateBreweryScheduleDto.java`
- 수정: `src/main/java/com/example/monghyang/domain/auth/dto/BreweryScheduleDto.java`
- 테스트: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`

- [ ] **Step 1: 휴게시간 제거 요청이 break row를 저장하지 않는 테스트를 추가한다**

`BreweryServiceTest`에 `never` static import를 추가한다.

```java
import static org.mockito.Mockito.never;
```

아래 테스트를 추가한다.

```java
@Test
@DisplayName("양조장 일정 변경에서 휴게시간이 비어 있으면 새 버전에 휴게시간 row를 저장하지 않는다")
void update_brewery_schedule_does_not_save_break_time_when_break_fields_are_empty() {
    Long userId = 1L;
    Long breweryId = 5L;
    ReqUpdateBreweryScheduleDto dto = updateScheduleDto();
    Brewery brewery = mock(Brewery.class);
    given(brewery.getId()).willReturn(breweryId);
    given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));

    breweryService.updateBrewerySchedule(userId, dto);

    verify(breweryWeeklyBreakTimeRepository).deleteByBreweryIdAndEffectiveDate(breweryId, dto.getEffective_date());
    verify(breweryWeeklyBreakTimeRepository, never()).save(any());
}
```

- [ ] **Step 2: 휴게 시작/종료 중 하나만 입력하면 거부하는 테스트를 추가한다**

`BreweryServiceTest`에 assertion import를 추가한다.

```java
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
```

아래 테스트를 추가한다.

```java
@Test
@DisplayName("양조장 일정 변경은 휴게 시작과 종료 중 하나만 입력되면 거부한다")
void update_brewery_schedule_rejects_partial_break_time() {
    Long userId = 1L;
    Long breweryId = 5L;
    ReqUpdateBreweryScheduleDto dto = updateScheduleDto();
    dto.getSchedules().getFirst().setBreak_start(LocalTime.of(12, 0));
    Brewery brewery = mock(Brewery.class);
    given(brewery.getId()).willReturn(breweryId);
    given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> breweryService.updateBrewerySchedule(userId, dto)
    );

    assertEquals(ApplicationError.BREWERY_OPENING_TIME_INVALID, exception.getApplicationError());
    verify(breweryWeeklyOpenTimeRepository, never()).save(any());
    verify(breweryWeeklyBreakTimeRepository, never()).save(any());
}
```

- [ ] **Step 3: 실패 확인**

실행:

```bash
./gradlew test --tests '*BreweryServiceTest'
```

기대 결과:

- Step 1 테스트는 현재도 통과할 수 있다.
- Step 2 테스트는 현재 구현이 한쪽 휴게시간만 있는 요청을 거부하지 않아 실패해야 한다.

- [ ] **Step 4: `BreweryService.updateBrewerySchedule` 검증을 수정한다**

휴게시간 검증 블록을 아래처럼 바꾼다.

```java
boolean hasBreakStart = schedule.getBreak_start() != null;
boolean hasBreakEnd = schedule.getBreak_end() != null;
if (hasBreakStart != hasBreakEnd) {
    throw new ApplicationException(ApplicationError.BREWERY_OPENING_TIME_INVALID);
}
if (hasBreakStart) {
    if (schedule.getBreak_start().isBefore(schedule.getOpen_time())
            || schedule.getBreak_end().isAfter(schedule.getClose_time())
            || !schedule.getBreak_start().isBefore(schedule.getBreak_end())) {
        throw new ApplicationException(ApplicationError.BREWERY_OPENING_TIME_INVALID);
    }
}
```

- [ ] **Step 5: DTO 주석을 전체 주간 스냅샷 의미로 갱신한다**

`ReqUpdateBreweryScheduleDto.schedules` 주석을 아래처럼 바꾼다.

```java
/**
 * 새 적용일부터 사용할 전체 요일별 운영/휴게시간 목록.
 * 목록에 없는 요일은 미운영으로 해석하고, 포함된 요일에서 휴게 시작/종료가 둘 다 없으면 휴게시간 없음으로 해석합니다.
 */
```

`BreweryScheduleDto` 필드 주석을 아래처럼 보강한다.

```java
/** 운영 요일입니다. */
@NotNull(message = "요일 정보를 입력해주세요.")
private DayOfWeek day_of_week;

/** 해당 요일의 운영 시작 시간입니다. */
@NotNull(message = "해당 요일의 운영 시작 시간을 입력해주세요.")
private LocalTime open_time;

/** 해당 요일의 운영 종료 시간입니다. */
@NotNull(message = "해당 요일의 운영 종료 시간을 입력해주세요.")
private LocalTime close_time;

/** 해당 요일의 휴게 시작 시간입니다. 휴게시간이 없으면 break_end와 함께 비웁니다. */
private LocalTime break_start;

/** 해당 요일의 휴게 종료 시간입니다. 휴게시간이 없으면 break_start와 함께 비웁니다. */
private LocalTime break_end;
```

- [ ] **Step 6: 테스트 통과 확인**

실행:

```bash
./gradlew test --tests '*BreweryServiceTest'
```

기대 결과:

```text
BUILD SUCCESSFUL
```

---

### Task 5: 체험 일정 변경 요청 의미 문서화

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyScheduleDto.java`
- 수정: `src/main/java/com/example/monghyang/domain/joy/dto/JoyScheduleDto.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/dto/JoyScheduleDtoTest.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`

- [ ] **Step 1: DTO 주석을 전체 주간 스냅샷 의미로 갱신한다**

`ReqUpdateJoyScheduleDto.schedules` 주석을 아래처럼 바꾼다.

```java
/** 새 적용일부터 저장할 전체 요일별 체험 시작 시간 목록입니다. 목록에 없는 요일은 체험 미운영으로 해석합니다. */
@Valid
@NotEmpty(message = "체험 일정 목록을 입력해주세요.")
private List<JoyScheduleDto> schedules;
```

`JoyScheduleDto.start_times` 주석을 아래처럼 바꾼다.

```java
/** 해당 운영 요일에 예약 가능한 체험 시작 시간 목록입니다. 미운영 요일은 이 DTO를 보내지 않습니다. */
@NotEmpty(message = "체험 시작 시간 목록을 입력해주세요.")
private List<@NotNull(message = "체험 시작 시간은 null일 수 없습니다.") LocalTime> start_times;
```

- [ ] **Step 2: 기존 DTO 검증 정책이 유지되는지 확인한다**

실행:

```bash
./gradlew test --tests '*JoyScheduleDtoTest'
```

기대 결과:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: 체험 일정 변경 저장 테스트가 유지되는지 확인한다**

실행:

```bash
./gradlew test --tests '*JoyServiceTest'
```

기대 결과:

```text
BUILD SUCCESSFUL
```

---

### Task 6: 전체 검증과 자기 검토

**파일:**
- 검증 대상: 변경된 모든 Java source/test 파일

- [ ] **Step 1: 컴파일 확인**

실행:

```bash
./gradlew compileJava
```

기대 결과:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 2: 관련 단위 테스트 확인**

실행:

```bash
./gradlew test --tests '*JoySlotServiceTest' --tests '*BreweryServiceTest' --tests '*JoyServiceTest' --tests '*JoyOrderServiceTest' --tests '*JoyScheduleDtoTest'
```

기대 결과:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: 전체 테스트 확인**

실행:

```bash
./gradlew test
```

기대 결과:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 4: 단순성 자기 검토**

아래 질문에 대해 변경 diff를 보며 확인한다.

- 새 테이블, 새 enum 값, sentinel time 없이 해결했는가?
- `effective_date`를 주간 버전으로 고르는 로직이 운영시간, 휴게시간, 체험 시작시간에 일관되게 적용되었는가?
- 휴게시간 없음과 체험 미운영을 row 부재로 표현하고 있는가?
- 전체 일정 없음처럼 스냅샷 헤더가 필요한 범위를 몰래 구현하지 않았는가?
- 테스트 편의를 위해 production visibility, constructor, setter, 불필요한 abstraction을 추가하지 않았는가?
- 변경 파일이 이번 스냅샷 조회 결함과 직접 관련된 파일로 제한되었는가?

- [ ] **Step 5: 미해결 위험 보고**

완료 보고에 아래 항목을 포함한다.

- `ReqUpdateJoyScheduleDto.schedules`가 빈 목록을 허용하지 않으므로 체험의 모든 요일을 한 번에 미운영으로 만드는 기능은 여전히 별도 설계가 필요하다.
- Repository JPQL은 Spring Data JPA 3.5.1 기준으로 작성하되, 이번 계획은 Context7 호출 없이 기존 프로젝트의 JPQL 패턴을 따른다.
- 코드 변경 커밋은 사용자 확인이 있어야 수행한다.
