# 양조장 휴게시간 스냅샷 예약 검증 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `BreweryWeeklyBreakTime` 스냅샷을 예약 가능 날짜 조회, 시간대 조회, 예약 생성/변경 검증에 반영해 휴게시간과 겹치는 체험 예약을 차단한다.

**Architecture:** 기존 `BreweryWeeklyOpenTime` 및 `JoyWeeklyStartTime` 스냅샷 조회 패턴을 `BreweryWeeklyBreakTime`에 동일하게 확장한다. 새 서비스 계층이나 공통 스케줄 추상화는 만들지 않고, `JoySlotService`와 `JoyOrderService`의 현재 검증 흐름 안에 휴게시간 차집합 검증만 추가한다. 휴게시간 판정은 체험 시작 시각만이 아니라 `reservationTime`부터 `reservationTime + timeUnit`까지의 체험 진행 구간이 휴게시간과 하나라도 겹치면 무효로 본다.

**Tech Stack:** Java 21, Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5, Mockito. 정확한 라이브러리 버전은 `docs/context7-dependencies.yaml` 기준이다.

---

## 승인 및 실행 경계

- 이 문서는 계획이며, 사용자가 이 계획을 명시 승인하기 전에는 프로덕션 코드와 테스트 코드를 수정하지 않는다.
- 구현 승인 후에는 `superpowers:using-git-worktrees`를 먼저 적용한다. 현재 브랜치가 `main` 또는 `master`이면 별도 작업 브랜치나 worktree를 사용한다.
- 구현은 `superpowers:executing-plans`로 진행한다. 변경 범위가 `BreweryWeeklyBreakTime` 조회, 예약 슬롯 조회 필터, 예약 검증에 집중되어 있어 subagent 기본 사용 대상은 아니다.
- 코드 변경 커밋은 별도 사용자 확인 전에는 만들지 않는다.

## 의존성 및 Context7 기준

- version: Spring Boot `3.5.3`, Spring Data JPA `3.5.1`, JUnit은 `spring-boot-starter-test 3.5.3` 기준
- source: `docs/context7-dependencies.yaml`
- context7_library_id: `not_used`
- reason: 기존 repository의 `@Query`, `@Modifying`, Mockito 단위 테스트 패턴을 그대로 확장하는 계획이며, 새로운 API signature나 버전별 옵션 확인이 필요하지 않다.

## 파일 구조

- Modify: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepository.java`
  - 날짜 기준 활성 휴게시간 조회와 월 범위 휴게시간 조회 쿼리를 추가한다.
- Create: `src/main/java/com/example/monghyang/domain/joy/dto/slot/UnavailableJoySlotTimeDto.java`
  - 월별 매진 슬롯의 날짜와 시간을 보존하기 위한 projection이다.
  - 새 projection이 필요한 이유: 휴게시간 슬롯을 제외한 뒤 매진 여부를 판단하려면 기존 일자별 count projection만으로는 어느 시간이 매진인지 알 수 없다.
- Modify: `src/main/java/com/example/monghyang/domain/joy/repository/JoySlotRepository.java`
  - 월별 매진 슬롯의 날짜와 시간을 조회하는 쿼리를 추가한다.
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
  - 예약 가능 날짜와 남은 자리 시간대 계산에서 휴게시간과 겹치는 슬롯을 제거한다.
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
  - 예약 생성, 사용자 예약 변경, 양조장 예약 변경 검증에서 휴게시간과 겹치는 예약을 거부한다.
- Modify: `src/main/java/com/example/monghyang/domain/brewery/dto/JoyInfoDto.java`
  - 기존 예약 검증 projection에 양조장 식별자를 포함해 휴게시간 조회에 재사용한다.
- Modify: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryRepository.java`
  - `findJoyTimeInfoByJoyId` projection이 `breweryId`를 함께 반환하도록 수정한다.
- Test: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
  - 휴게시간 때문에 하루 전체가 예약 불가가 되는 경우와 일부 시간대만 숨겨지는 경우를 검증한다.
- Test: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
  - 휴게시간과 겹치는 예약 생성/변경이 `JOY_ORDER_TIME_INVALID`로 거부되는지 검증한다.
- Test: `src/test/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepositoryTest.java`
  - 새 repository 메서드 signature가 서비스에서 요구하는 인자를 그대로 받는지 검증한다. 현재 저장소의 repository 테스트가 mock 기반이므로 같은 범위로 맞춘다.

---

### Task 0: 실행 전 저장소 규칙 확인

**Files:**
- Read: `docs/context7-dependencies.yaml`
- Read: `docs/junit-unit-test-guide.md`
- Inspect: `git status`

- [ ] **Step 0.1: 의존성 및 테스트 기준 문서 확인**

Run:

```bash
find docs -maxdepth 4 -type f
```

Expected:

```text
docs/context7-dependencies.yaml
docs/junit-unit-test-guide.md
```

- [ ] **Step 0.2: 현재 브랜치와 작업 트리 확인**

Run:

```bash
git status --short --branch
```

Expected:

```text
main 또는 master가 아니어야 한다.
사용자 변경이 있으면 보존하고, 이번 계획 범위 파일만 수정한다.
```

- [ ] **Step 0.3: worktree 규칙 적용**

구현 승인 후 `superpowers:using-git-worktrees`를 사용한다. 별도 worktree 생성이 필요하면 `codex/` prefix 브랜치 또는 저장소의 현재 작업 브랜치 정책을 따른다.

---

### Task 1: 휴게시간 조회 repository 추가

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepository.java`
- Test: `src/test/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepositoryTest.java`

- [ ] **Step 1.1: 실패 테스트 작성**

Create `src/test/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepositoryTest.java`:

```java
package com.example.monghyang.domain.brewery.repository;

import com.example.monghyang.domain.global.DayOfWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BreweryWeeklyBreakTimeRepositoryTest {

    @Test
    @DisplayName("예약일에 활성화된 양조장 휴게시간 스냅샷을 조회한다")
    void find_active_break_times_by_brewery_id_and_date() {
        BreweryWeeklyBreakTimeRepository repository = mock(BreweryWeeklyBreakTimeRepository.class);
        Long breweryId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 6, 1);

        repository.findActiveBreakTimesByBreweryIdAndDate(breweryId, targetDate, DayOfWeek.Mon);

        verify(repository).findActiveBreakTimesByBreweryIdAndDate(breweryId, targetDate, DayOfWeek.Mon);
    }

    @Test
    @DisplayName("월 범위에서 유효한 양조장 휴게시간 스냅샷을 조회한다")
    void find_active_and_future_break_times_in_month() {
        BreweryWeeklyBreakTimeRepository repository = mock(BreweryWeeklyBreakTimeRepository.class);
        Long breweryId = 1L;
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 1);

        repository.findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate);

        verify(repository).findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate);
    }
}
```

Run:

```bash
./gradlew test --tests '*BreweryWeeklyBreakTimeRepositoryTest'
```

Expected:

```text
컴파일 실패: findActiveBreakTimesByBreweryIdAndDate 또는 findActiveAndFutureBreakTimesInMonth 메서드가 존재하지 않는다.
```

- [ ] **Step 1.2: repository 쿼리 구현**

Add to `BreweryWeeklyBreakTimeRepository`:

```java
import com.example.monghyang.domain.global.DayOfWeek;

import java.util.List;
```

Add methods:

```java
/**
 * 예약일 기준으로 활성화된 특정 요일의 양조장 휴게시간을 조회합니다.
 *
 * @param breweryId  양조장 식별자
 * @param targetDate 예약 대상일
 * @param dayOfWeek  예약 대상일의 요일
 * @return 예약일에 적용되는 양조장 휴게시간 목록
 */
@Query("""
    select bbt from BreweryWeeklyBreakTime bbt
    where bbt.brewery.id = :breweryId
      and bbt.dayOfWeek = :dayOfWeek
      and bbt.effectiveDate = (
          select max(bbt2.effectiveDate)
          from BreweryWeeklyBreakTime bbt2
          where bbt2.brewery.id = :breweryId
            and bbt2.dayOfWeek = :dayOfWeek
            and bbt2.effectiveDate <= :targetDate
      )
""")
List<BreweryWeeklyBreakTime> findActiveBreakTimesByBreweryIdAndDate(
        @Param("breweryId") Long breweryId,
        @Param("targetDate") LocalDate targetDate,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
);

/**
 * 특정 월 범위에서 시작일 기준 활성 스냅샷부터 종료일 이전 스냅샷까지의 양조장 휴게시간 목록을 조회합니다.
 *
 * @param breweryId 양조장 식별자
 * @param startDate 조회 시작일
 * @param endDate   조회 종료일
 * @return 월 범위 계산에 필요한 양조장 휴게시간 목록
 */
@Query("""
    select bbt from BreweryWeeklyBreakTime bbt
    where bbt.brewery.id = :breweryId
      and bbt.effectiveDate < :endDate
      and bbt.effectiveDate >= coalesce(
          (select max(bbt2.effectiveDate)
           from BreweryWeeklyBreakTime bbt2
           where bbt2.brewery.id = :breweryId
             and bbt2.effectiveDate <= :startDate),
          bbt.effectiveDate
      )
""")
List<BreweryWeeklyBreakTime> findActiveAndFutureBreakTimesInMonth(
        @Param("breweryId") Long breweryId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
);
```

- [ ] **Step 1.3: repository 테스트 통과 확인**

Run:

```bash
./gradlew test --tests '*BreweryWeeklyBreakTimeRepositoryTest'
```

Expected:

```text
BreweryWeeklyBreakTimeRepositoryTest PASS
```

---

### Task 2: 월별 매진 슬롯 projection 추가

**Files:**
- Create: `src/main/java/com/example/monghyang/domain/joy/dto/slot/UnavailableJoySlotTimeDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/repository/JoySlotRepository.java`

- [ ] **Step 2.1: projection 생성**

Create `UnavailableJoySlotTimeDto`:

```java
package com.example.monghyang.domain.joy.dto.slot;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 예약 인원이 최대 수용 인원에 도달한 체험 슬롯의 날짜와 시간을 나타냅니다.
 */
public interface UnavailableJoySlotTimeDto {
    /**
     * 매진된 체험 예약일입니다.
     */
    LocalDate getReservationDate();

    /**
     * 매진된 체험 시작 시간입니다.
     */
    LocalTime getReservationTime();
}
```

- [ ] **Step 2.2: `JoySlotRepository` 월별 매진 시간 조회 추가**

Add import:

```java
import com.example.monghyang.domain.joy.dto.slot.UnavailableJoySlotTimeDto;
```

Add method:

```java
/**
 * 한 달 동안 예약 인원이 꽉 찬 시간대를 날짜와 시간 단위로 조회합니다.
 *
 * @param joyId     체험 식별자
 * @param startDate 조회 시작일
 * @param endDate   조회 종료일
 * @return 매진된 날짜와 시작 시간 목록
 */
@Query("""
select js.reservationDate reservationDate, js.reservationTime reservationTime from JoySlot js
where js.joy.id = :joyId and js.reservationDate >= :startDate and js.reservationDate < :endDate
and js.count >= (select j.maxCount from Joy j where j.id = :joyId)
""")
List<UnavailableJoySlotTimeDto> findUnavailableJoySlotTimesByJoyIdAndMonth(
        @Param("joyId") Long joyId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
);
```

- [ ] **Step 2.3: 컴파일 확인**

Run:

```bash
./gradlew compileJava
```

Expected:

```text
BUILD SUCCESSFUL
```

---

### Task 3: `JoySlotService` 예약 가능 슬롯 계산에 휴게시간 반영

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
- Test: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`

- [ ] **Step 3.1: 실패 테스트 작성**

Create `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`:

```java
package com.example.monghyang.domain.joy.service;

import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyOpenTime;
import com.example.monghyang.domain.brewery.repository.BreweryClosedDateRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
import com.example.monghyang.domain.global.ClosedStatus;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.joy.dto.slot.ReqFindJoySlotDateDto;
import com.example.monghyang.domain.joy.dto.slot.ResJoySlotDateDto;
import com.example.monghyang.domain.joy.dto.slot.ResJoySlotTimeDto;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoySlot;
import com.example.monghyang.domain.joy.entity.JoyWeeklyStartTime;
import com.example.monghyang.domain.joy.repository.JoyClosedDateRepository;
import com.example.monghyang.domain.joy.repository.JoyClosedStartTimeRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoySlotRepository;
import com.example.monghyang.domain.joy.repository.JoyWeeklyStartTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JoySlotServiceTest {
    @Mock JoySlotRepository joySlotRepository;
    @Mock JoyRepository joyRepository;
    @Mock JoyWeeklyStartTimeRepository joyWeeklyStartTimeRepository;
    @Mock BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    @Mock BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @Mock BreweryClosedDateRepository breweryClosedDateRepository;
    @Mock JoyClosedDateRepository joyClosedDateRepository;
    @Mock JoyClosedStartTimeRepository joyClosedStartTimeRepository;
    @InjectMocks JoySlotService joySlotService;

    @Test
    @DisplayName("예약 가능 날짜 조회는 휴게시간과 겹치는 시작 시간을 유효 슬롯에서 제외한다")
    void get_impossible_date_excludes_break_time_slots() {
        Long joyId = 10L;
        Long breweryId = 20L;
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 1);
        ReqFindJoySlotDateDto dto = new ReqFindJoySlotDateDto();
        dto.setJoyId(joyId);
        dto.setYear(2026);
        dto.setMonth(6);
        Joy joy = joy(breweryId, 60, 10);

        given(joyRepository.findById(joyId)).willReturn(Optional.of(joy));
        given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, startDate, endDate))
                .willReturn(List.of(openTime(LocalTime.of(9, 0), LocalTime.of(18, 0))));
        given(joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, startDate, endDate))
                .willReturn(List.of(startTime(LocalTime.of(12, 0))));
        given(breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate))
                .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));
        given(breweryClosedDateRepository.findConfirmedByBreweryIdAndMonth(breweryId, startDate, endDate, ClosedStatus.CONFIRMED))
                .willReturn(List.of());
        given(joyClosedDateRepository.findConfirmedByJoyIdAndMonth(joyId, startDate, endDate, ClosedStatus.CONFIRMED))
                .willReturn(List.of());
        given(joyClosedStartTimeRepository.findConfirmedByJoyIdAndMonth(joyId, startDate, endDate, ClosedStatus.CONFIRMED))
                .willReturn(List.of());
        given(joySlotRepository.findUnavailableJoySlotTimesByJoyIdAndMonth(joyId, startDate, endDate))
                .willReturn(List.of());

        ResJoySlotDateDto result = joySlotService.getImpossibleDate(dto);

        assertTrue(result.getJoy_unavailable_reservation_date().contains(LocalDate.of(2026, 6, 1)));
    }

    @Test
    @DisplayName("남은 자리 조회는 휴게시간과 겹치는 시작 시간을 응답에서 제외한다")
    void get_remaining_count_list_excludes_break_time_slots() {
        Long joyId = 10L;
        Long breweryId = 20L;
        LocalDate targetDate = LocalDate.of(2026, 6, 1);
        Joy joy = joy(breweryId, 60, 10);

        given(joyRepository.findById(joyId)).willReturn(Optional.of(joy));
        given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
                .willReturn(List.of(openTime(LocalTime.of(9, 0), LocalTime.of(18, 0))));
        given(joyWeeklyStartTimeRepository.findActiveAndFutureStartTimesInMonth(joyId, targetDate, targetDate.plusDays(1)))
                .willReturn(List.of(startTime(LocalTime.of(10, 0)), startTime(LocalTime.of(12, 0))));
        given(breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, targetDate, targetDate.plusDays(1)))
                .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));
        given(joySlotRepository.findByJoyIdAndDate(joyId, targetDate))
                .willReturn(List.of(
                        JoySlot.joyReservationOf(joy, targetDate, LocalTime.of(10, 0), 2),
                        JoySlot.joyReservationOf(joy, targetDate, LocalTime.of(12, 0), 2)
                ));

        ResJoySlotTimeDto result = joySlotService.getRemainingCountList(joyId, targetDate);

        assertEquals(List.of(LocalTime.of(10, 0)), result.getTime_info());
        assertEquals(1, result.getRemaining_count_list().size());
        assertEquals(LocalTime.of(10, 0), result.getRemaining_count_list().getFirst().getJoy_slot_reservation_time());
    }

    private Joy joy(Long breweryId, Integer timeUnit, Integer maxCount) {
        Brewery brewery = mock(Brewery.class);
        Joy joy = mock(Joy.class);
        given(brewery.getId()).willReturn(breweryId);
        given(joy.getBrewery()).willReturn(brewery);
        given(joy.getTimeUnit()).willReturn(timeUnit);
        given(joy.getMaxCount()).willReturn(maxCount);
        return joy;
    }

    private BreweryWeeklyOpenTime openTime(LocalTime openTime, LocalTime closeTime) {
        return BreweryWeeklyOpenTime.builder()
                .brewery(mock(Brewery.class))
                .dayOfWeek(DayOfWeek.Mon)
                .openTime(openTime)
                .closeTime(closeTime)
                .effectiveDate(LocalDate.of(2026, 6, 1))
                .build();
    }

    private JoyWeeklyStartTime startTime(LocalTime startTime) {
        return JoyWeeklyStartTime.joyDayOfWeekStartTimeEffectiveDateOf(
                mock(Joy.class),
                DayOfWeek.Mon,
                startTime,
                LocalDate.of(2026, 6, 1)
        );
    }

    private BreweryWeeklyBreakTime breakTime(LocalTime breakStart, LocalTime breakEnd) {
        return BreweryWeeklyBreakTime.builder()
                .brewery(mock(Brewery.class))
                .dayOfWeek(DayOfWeek.Mon)
                .breakStart(breakStart)
                .breakEnd(breakEnd)
                .effectiveDate(LocalDate.of(2026, 6, 1))
                .build();
    }
}
```

Run:

```bash
./gradlew test --tests '*JoySlotServiceTest'
```

Expected:

```text
컴파일 실패 또는 검증 실패: JoySlotService에 BreweryWeeklyBreakTimeRepository 의존성과 휴게시간 필터가 없다.
```

- [ ] **Step 3.2: `JoySlotService` 의존성 및 helper 추가**

Add import:

```java
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
```

Add field:

```java
private final BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
```

Add helper methods:

```java
/**
 * 특정 날짜와 요일에 해당하는 양조장 휴게시간 목록을 전체 스냅샷 이력 중에서 조회합니다.
 *
 * @param breakTimeList 양조장 휴게시간 스냅샷 리스트
 * @param date          예약 대상 날짜
 * @param dayOfWeek     예약 대상 요일
 * @return 해당 날짜에 유효한 휴게시간 목록
 */
private List<BreweryWeeklyBreakTime> findActiveBreakTimes(List<BreweryWeeklyBreakTime> breakTimeList, LocalDate date, DayOfWeek dayOfWeek) {
    List<BreweryWeeklyBreakTime> candidates = breakTimeList.stream()
            .filter(b -> b.getDayOfWeek() == dayOfWeek && !b.getEffectiveDate().isAfter(date))
            .toList();
    if (candidates.isEmpty()) {
        return List.of();
    }
    LocalDate maxEffectiveDate = candidates.stream()
            .map(BreweryWeeklyBreakTime::getEffectiveDate)
            .max(LocalDate::compareTo)
            .orElseThrow();
    return candidates.stream()
            .filter(b -> b.getEffectiveDate().equals(maxEffectiveDate))
            .toList();
}

/**
 * 체험 진행 시간이 양조장 휴게시간과 겹치는지 확인합니다.
 *
 * @param startTime  체험 시작 시간
 * @param timeUnit   체험 진행 시간 단위
 * @param breakTimes 해당 날짜의 유효 휴게시간 목록
 * @return 휴게시간과 겹치면 true
 */
private boolean overlapsBreakTime(LocalTime startTime, Integer timeUnit, List<BreweryWeeklyBreakTime> breakTimes) {
    LocalTime endTime = startTime.plusMinutes(timeUnit);
    return breakTimes.stream()
            .anyMatch(b -> startTime.isBefore(b.getBreakEnd()) && endTime.isAfter(b.getBreakStart()));
}
```

- [ ] **Step 3.3: `getImpossibleDate`에 휴게시간과 매진 시간 기준 반영**

Change bulk load section:

```java
List<BreweryWeeklyBreakTime> breakTimeList = breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(breweryId, startDate, endDate);
List<UnavailableJoySlotTimeDto> unavailableSlotTimes = joySlotRepository.findUnavailableJoySlotTimesByJoyIdAndMonth(
        dto.getJoyId(), startDate, endDate);
```

Build full slot map:

```java
Map<LocalDate, Set<LocalTime>> fullSlotTimesMap = unavailableSlotTimes.stream()
        .collect(Collectors.groupingBy(
                UnavailableJoySlotTimeDto::getReservationDate,
                Collectors.mapping(UnavailableJoySlotTimeDto::getReservationTime, Collectors.toSet())
        ));
```

Filter active slots:

```java
List<BreweryWeeklyBreakTime> activeBreakTimes = findActiveBreakTimes(breakTimeList, date, dayOfWeek);
List<LocalTime> activeSlots = startTimes.stream()
        .map(JoyWeeklyStartTime::getStartTime)
        .filter(t -> !t.isBefore(openTime) && !t.plusMinutes(joy.getTimeUnit()).isAfter(closeTime))
        .filter(t -> !overlapsBreakTime(t, joy.getTimeUnit(), activeBreakTimes))
        .toList();
```

Replace count comparison:

```java
Set<LocalTime> fullSlotTimes = fullSlotTimesMap.getOrDefault(date, Set.of());
boolean allValidSlotsFull = validSlots.stream().allMatch(fullSlotTimes::contains);

if (allValidSlotsFull) {
    result.getJoy_unavailable_reservation_date().add(date);
}
```

검토 기준:

- 기존 `UnavailableJoySlotTimeCountDto`와 `findUnavailableJoySlotTimeCountByJoyIdAndMonth`는 다른 호출자가 있을 수 있으므로 삭제하지 않는다.
- `!t.plusMinutes(joy.getTimeUnit()).isAfter(closeTime)`는 기존 예약 검증의 `reservationTime.plusMinutes(timeUnit).isAfter(endTime)`와 같은 의미로 종료 시간이 영업 종료 시간과 같을 수 있게 한다.

- [ ] **Step 3.4: `getRemainingCountList`에 휴게시간 반영**

Load break times:

```java
List<BreweryWeeklyBreakTime> breakTimeList = breweryWeeklyBreakTimeRepository.findActiveAndFutureBreakTimesInMonth(joy.getBrewery().getId(), targetDate, limitDate);
List<BreweryWeeklyBreakTime> activeBreakTimes = findActiveBreakTimes(breakTimeList, targetDate, dayOfWeek);
```

Filter response times:

```java
List<LocalTime> activeStartTimes = startTimes.stream()
        .map(JoyWeeklyStartTime::getStartTime)
        .filter(t -> !t.isBefore(openTime) && !t.plusMinutes(joy.getTimeUnit()).isAfter(closeTime))
        .filter(t -> !overlapsBreakTime(t, joy.getTimeUnit(), activeBreakTimes))
        .sorted()
        .toList();
Set<LocalTime> responseTimes = Set.copyOf(activeStartTimes);
```

Filter remaining count list:

```java
for (JoySlot joySlot : joySlotList) {
    if (!responseTimes.contains(joySlot.getReservationTime())) {
        continue;
    }
    result.getRemaining_count_list().add(JoySlotTimeCountDto.timeCountOf(
            joySlot.getReservationTime(),
            joy.getMaxCount() - joySlot.getCount()
    ));
}
```

- [ ] **Step 3.5: `JoySlotService` 테스트 통과 확인**

Run:

```bash
./gradlew test --tests '*JoySlotServiceTest'
```

Expected:

```text
JoySlotServiceTest PASS
```

---

### Task 4: `JoyOrderService` 예약 검증에 휴게시간 반영

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- Test: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`

- [ ] **Step 4.1: 실패 테스트 추가**

Add tests to `JoyOrderServiceTest`:

```java
import com.example.monghyang.domain.brewery.entity.Brewery;
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
```

Add mock field:

```java
@Mock
BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
```

Add test:

```java
@Test
@DisplayName("예약 슬롯 증가는 양조장 휴게시간과 겹치는 시간대를 거부한다")
void reservation_joy_slot_count_rejects_break_time_overlap() {
    Long joyId = 10L;
    Long breweryId = 20L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(12, 30);

    given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
            .willReturn(Optional.of(new JoyInfoDto(breweryId, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
    given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(breweryId, reservationDate, DayOfWeek.Mon))
            .willReturn(List.of(breakTime(LocalTime.of(12, 0), LocalTime.of(13, 0))));

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.reservationJoySlotCount(joyId, reservationDate, reservationTime, 2)
    );

    assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
    verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
}
```

필요한 helper:

```java
private BreweryWeeklyBreakTime breakTime(LocalTime breakStart, LocalTime breakEnd) {
    return BreweryWeeklyBreakTime.builder()
            .brewery(mock(Brewery.class))
            .dayOfWeek(DayOfWeek.Mon)
            .breakStart(breakStart)
            .breakEnd(breakEnd)
            .effectiveDate(LocalDate.of(2026, 6, 1))
            .build();
}
```

Run:

```bash
./gradlew test --tests '*JoyOrderServiceTest'
```

Expected:

```text
컴파일 실패 또는 검증 실패: JoyInfoDto에 breweryId가 없거나 JoyOrderService에 BreweryWeeklyBreakTimeRepository 의존성과 휴게시간 검증이 없다.
```

- [ ] **Step 4.2: `JoyInfoDto`와 `BreweryRepository.findJoyTimeInfoByJoyId` projection 수정**

Change `JoyInfoDto`:

```java
public record JoyInfoDto(
        Long breweryId,
        LocalTime breweryStartTime,
        LocalTime breweryEndTime,
        Integer timeUnit,
        Integer maxCount,
        Integer minCount
) {}
```

Change `BreweryRepository.findJoyTimeInfoByJoyId` projection:

```java
select new com.example.monghyang.domain.brewery.dto.JoyInfoDto(
    b.id, wot.openTime, wot.closeTime, j.timeUnit, j.maxCount, j.minCount)
```

그리고 `JoyOrderServiceTest`에서 `JoyInfoDto`를 직접 생성하는 기존 테스트 데이터를 모두 `new JoyInfoDto(breweryId, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)` 형태로 갱신한다.

이 방식을 쓰는 이유: `findJoyTimeInfoByJoyId`가 이미 `Joy -> Brewery -> BreweryWeeklyOpenTime`을 조인하므로, 휴게시간 조회에 필요한 `breweryId`를 같은 projection에서 가져오는 것이 별도 repository 조회를 추가하는 것보다 단순하고 효율적이다.

- [ ] **Step 4.3: `JoyOrderService` 의존성과 helper 추가**

Add imports:

```java
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyBreakTime;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
```

Add field:

```java
private final BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
```

Add helper:

```java
/**
 * 체험 진행 시간이 양조장 휴게시간과 겹치면 예약 불가 예외를 발생시킵니다.
 *
 * @param breweryId       양조장 식별자
 * @param reservationDate 예약 대상일
 * @param reservationTime 예약 시작 시간
 * @param timeUnit        체험 진행 시간 단위
 * @param dayOfWeek       예약 대상일의 요일
 */
private void verifyBreakTime(Long breweryId, LocalDate reservationDate, LocalTime reservationTime, Integer timeUnit, DayOfWeek dayOfWeek) {
    LocalTime reservationEndTime = reservationTime.plusMinutes(timeUnit);
    boolean overlapsBreakTime = breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(breweryId, reservationDate, dayOfWeek)
            .stream()
            .anyMatch(b -> reservationTime.isBefore(b.getBreakEnd()) && reservationEndTime.isAfter(b.getBreakStart()));
    if (overlapsBreakTime) {
        throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
    }
}
```

- [ ] **Step 4.4: `verifyReservation`에서 휴게시간 검증 호출**

Insert after operating-time validation and before `JoyWeeklyStartTime` validation:

```java
DayOfWeek dayOfWeek = DayOfWeek.from(reservationDate.getDayOfWeek());
verifyBreakTime(joyInfoDto.breweryId(), reservationDate, reservationTime, joyInfoDto.timeUnit(), dayOfWeek);
```

Then reuse that `dayOfWeek` variable for `findActiveStartTimesByJoyIdAndDate`.

- [ ] **Step 4.5: `JoyOrderService` 테스트 통과 확인**

Run:

```bash
./gradlew test --tests '*JoyOrderServiceTest'
```

Expected:

```text
JoyOrderServiceTest PASS
```

---

### Task 5: 전체 검증 및 self-review

**Files:**
- Review: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepository.java`
- Review: `src/main/java/com/example/monghyang/domain/joy/repository/JoySlotRepository.java`
- Review: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
- Review: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- Review: related tests

- [ ] **Step 5.1: 관련 테스트 실행**

Run:

```bash
./gradlew test --tests '*BreweryWeeklyBreakTimeRepositoryTest' --tests '*JoySlotServiceTest' --tests '*JoyOrderServiceTest'
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5.2: 전체 테스트 실행**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5.3: self-review 체크**

검토 질문:

- 휴게시간과 겹치는 예약이 생성, 사용자 변경, 양조장 변경 모두에서 차단되는가?
- 예약 가능 날짜와 남은 자리 시간대 조회가 같은 휴게시간 판정 함수를 사용하는가?
- 기존 `JoyInfoDto`나 별도 스케줄 추상화를 넓히지 않고 최소 변경으로 해결했는가?
- 일자별 매진 count 방식이 휴게시간 제외 후에도 false positive를 만들지 않도록 시간 단위 projection으로 바뀌었는가?
- 테스트 편의를 위해 프로덕션 설계를 왜곡한 setter, test-only constructor, 불필요한 visibility 변경을 추가하지 않았는가?

- [ ] **Step 5.4: 완료 보고**

보고에는 다음을 포함한다.

```text
변경 파일:
- src/main/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepository.java
- src/main/java/com/example/monghyang/domain/joy/dto/slot/UnavailableJoySlotTimeDto.java
- src/main/java/com/example/monghyang/domain/joy/repository/JoySlotRepository.java
- src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java
- src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java
- src/main/java/com/example/monghyang/domain/brewery/dto/JoyInfoDto.java
- src/main/java/com/example/monghyang/domain/brewery/repository/BreweryRepository.java
- src/test/java/com/example/monghyang/domain/brewery/repository/BreweryWeeklyBreakTimeRepositoryTest.java
- src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java
- src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java

구현 동작:
- 휴게시간과 체험 진행 시간이 겹치면 예약 생성 및 예약 변경을 거부한다.
- 예약 가능 날짜 조회와 남은 자리 조회에서 휴게시간과 겹치는 시작 시간을 제외한다.
- 휴게시간 제외 후 매진 여부는 시간 단위 매진 projection으로 계산한다.

검증:
- ./gradlew test --tests '*BreweryWeeklyBreakTimeRepositoryTest' --tests '*JoySlotServiceTest' --tests '*JoyOrderServiceTest'
- ./gradlew test

self-review:
- 새 공통 abstraction 없이 기존 서비스와 repository 패턴만 확장했다.
- 테스트 편의를 위한 production visibility 완화나 test-only constructor를 추가하지 않았다.
- `JoyInfoDto`에 `breweryId`만 추가해 이미 수행 중인 조인 결과를 재사용했고, 휴게시간 검증을 위한 별도 DB 조회를 만들지 않았다.

알려진 위험:
- repository 테스트는 현재 저장소 관례에 맞춰 mock signature 검증 중심이다. JPQL 의미 검증이 필요하면 Testcontainers 기반 repository 통합 테스트를 별도 승인 후 추가한다.
```

---

## 계획 self-review 결과

- `[결함1]`의 세 경로인 예약 가능 날짜 조회, 남은 자리 시간대 조회, 예약 생성/변경 검증을 모두 작업에 포함했다.
- 휴게시간 제외 후 매진 여부 계산이 기존 count projection으로는 부정확해질 수 있어, 시간 단위 projection을 추가하는 이유를 명시했다.
- 새 서비스, 새 공통 abstraction, 새 dependency는 추가하지 않는다.
- `JoyInfoDto`에 `breweryId`를 추가해 기존 조회 결과를 재사용하므로 예약 검증 단계의 추가 repository 왕복을 피한다.
- Context7은 사용하지 않았다. 정확한 외부 API signature 확인이 아니라 기존 Spring Data JPA 패턴 확장이기 때문이다.
