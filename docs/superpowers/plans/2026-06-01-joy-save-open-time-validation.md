# 체험 저장 운영시간 검증 구현 계획

> **agentic worker 필수 하위 스킬:** 이 계획을 구현할 때는 `superpowers:executing-plans`를 사용한다. 구현 전에는 `superpowers:using-git-worktrees`를 적용하고, 각 단계는 체크박스(`- [ ]`)로 추적한다.

**목표:** `/api/brewery-priv/joy-add`, `/api/brewery-priv/joy/schedule`이 체험 시작 시간 스냅샷을 저장하기 전에 양조장 운영시간 안에 있는지 검증하도록 한다.

**아키텍처:** 새 계층을 만들지 않고 `JoyService` 저장 전 검증에 운영시간 범위 검증을 추가한다. 운영시간 조회는 기존 `BreweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(...)`와 `JoySlotService`의 최신 주간 스냅샷 선택 규칙을 재사용한다. 저장 API는 조회/예약 API와 동일하게 `openTime <= startTime`이고 `startTime + timeUnit <= closeTime`인 시작 시간만 허용한다.

**version:** Java 21, Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5.12.2, Mockito 5.17.0/5.14.2.

**source:** `docs/context7-dependencies.yaml`

**context7_library_id:** `not_used` - 정확한 외부 API signature나 버전별 설정값 확인이 아니라 기존 repository 메서드, JPQL 패턴, JUnit/Mockito 테스트 스타일을 재사용하는 작업이므로 호출하지 않는다.

---

## 범위와 전제

- 저장 API의 거부 오류는 기존 체험 일정 검증과 같은 `ApplicationError.INVALID_TIME`을 사용한다.
- 양조장 운영시간이 없는 요일에 체험 시작 시간을 저장하려는 요청은 거부한다.
- 체험 시작 시간이 운영 시작과 같은 것은 허용한다.
- 체험 종료 시간이 운영 종료와 같은 것은 허용한다.
- 체험 시작 시간이 운영 시작보다 빠르거나, 체험 종료 시간이 운영 종료보다 늦으면 거부한다.
- 휴게시간 충돌 검증은 유지하고, 운영시간 검증을 그 앞에 추가한다.
- DB 스키마, DTO, controller mapping, 예약 검증 로직은 변경하지 않는다.
- 이미 저장된 과거의 잘못된 `JoyWeeklyStartTime` 데이터 정리는 이 계획 범위에 포함하지 않는다.
- 구현 전 승인 없이는 production code를 수정하지 않는다.

## 근거

- `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java:49`와 `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java:88`은 저장 전에 휴게시간 충돌만 검증한다.
- `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java:235`의 `validateNotOverlappingBreakTimes(...)`는 운영시간 범위를 확인하지 않는다.
- `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java:257`은 캘린더 조회에서 `!t.isBefore(openTime) && !t.plusMinutes(joy.getTimeUnit()).isAfter(closeTime)` 조건으로 운영시간 밖 시작 시간을 제외한다.
- `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java:86`은 예약 생성/변경에서 운영 시작 이전 또는 체험 종료가 운영 종료 이후인 요청을 거부한다.

## 파일 구조

- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
  - 양조장 운영시간 repository 의존성을 추가한다.
  - 체험 생성과 일정 변경 저장 전에 운영시간 범위 검증을 수행한다.
  - 최신 주간 운영시간 버전 선택 helper를 private 메서드로 둔다.
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
  - 운영시간 repository mock을 추가한다.
  - 기존 성공 테스트가 운영시간 검증을 통과하도록 스텁을 보강한다.
  - 운영시간 밖 시작 시간과 최신 운영시간 버전에 없는 요일을 거부하는 회귀 테스트를 추가한다.

---

### Task 0: 구현 전 규칙 확인

**Files:**
- 확인: `docs/context7-dependencies.yaml`
- 확인: `docs/junit-unit-test-guide.md`
- 확인: `git status`

- [ ] **Step 0.1: 의존성 기준과 테스트 작성 기준 확인**

Run:

```bash
sed -n '1,340p' docs/context7-dependencies.yaml
sed -n '1,260p' docs/junit-unit-test-guide.md
```

Expected:

```text
Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5.12.2, Mockito 5.17.0/5.14.2 기준을 확인한다.
JUnit 단위 테스트는 MockitoExtension 기반의 빠른 단위 테스트로 작성한다.
```

- [ ] **Step 0.2: 구현 작업 격리 확인**

Run:

```bash
git status --short
git branch --show-current
```

Expected:

```text
사용자 변경이 있으면 보존한다.
main 또는 master이면 구현 전에 superpowers:using-git-worktrees를 적용해 별도 작업 공간이나 브랜치에서 진행한다.
```

---

### Task 1: 저장 전 운영시간 검증 실패 테스트 추가

**Files:**
- Modify: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`

- [ ] **Step 1.1: 운영시간 repository mock과 helper를 추가한다**

`JoyServiceTest` import에 아래 항목을 추가한다.

```java
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyOpenTime;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
```

`JoyServiceTest`의 mock 필드에 아래 항목을 추가한다.

```java
@Mock
BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
```

`JoyServiceTest` 하단 helper 영역에 아래 helper를 추가한다.

```java
private void givenOpenTimes(Long breweryId, LocalDate effectiveDate, BreweryWeeklyOpenTime... openTimes) {
    given(breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(
            breweryId,
            effectiveDate,
            effectiveDate.plusDays(1)
    )).willReturn(List.of(openTimes));
}

private BreweryWeeklyOpenTime openTime(DayOfWeek dayOfWeek, LocalDate effectiveDate, LocalTime openTime, LocalTime closeTime) {
    return BreweryWeeklyOpenTime.builder()
            .brewery(brewery())
            .dayOfWeek(dayOfWeek)
            .openTime(openTime)
            .closeTime(closeTime)
            .effectiveDate(effectiveDate)
            .build();
}
```

- [ ] **Step 1.2: 기존 체험 생성 성공 테스트에 운영시간 스텁을 보강한다**

`create_joy_saves_initial_weekly_start_time_snapshot()`의 arrange 영역에 brewery id와 운영시간 스텁을 추가한다.

```java
Brewery brewery = brewery();
ReflectionTestUtils.setField(brewery, "id", 5L);
ReqJoyDto dto = reqJoyDto();
givenOpenTimes(
        5L,
        LocalDate.now(),
        openTime(DayOfWeek.Mon, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(18, 0)),
        openTime(DayOfWeek.Tue, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(18, 0))
);
given(breweryRepository.findActiveByUserId(userId)).willReturn(Optional.of(brewery));
```

- [ ] **Step 1.3: 기존 체험 일정 변경 성공 테스트에 운영시간 스텁을 보강한다**

`update_joy_schedule_allows_deleted_brewery_owner()`와 `update_joy_schedule_replaces_snapshot_and_requests_refund()`의 arrange 영역에 아래 스텁을 추가한다.

```java
givenOpenTimes(
        5L,
        effectiveDate,
        openTime(DayOfWeek.Mon, effectiveDate, LocalTime.of(9, 0), LocalTime.of(18, 0))
);
```

- [ ] **Step 1.4: 기존 휴게시간 충돌 테스트에 운영시간 스텁을 보강한다**

`create_joy_rejects_start_time_overlapping_break_time()`의 arrange 영역에 아래 스텁을 추가한다.

```java
givenOpenTimes(
        5L,
        LocalDate.now(),
        openTime(DayOfWeek.Mon, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(18, 0))
);
```

`update_joy_schedule_rejects_start_time_overlapping_break_time()`의 arrange 영역에 아래 스텁을 추가한다.

```java
givenOpenTimes(
        5L,
        effectiveDate,
        openTime(DayOfWeek.Mon, effectiveDate, LocalTime.of(9, 0), LocalTime.of(18, 0))
);
```

- [ ] **Step 1.5: 체험 생성이 운영시간 밖 시작 시간을 저장하지 않는 실패 테스트를 추가한다**

`JoyServiceTest`에 아래 테스트를 추가한다. 현재 구현은 운영시간 검증이 없어 `joyRepository.save(...)`까지 진행하므로 실패해야 한다.

```java
@Test
@DisplayName("체험 생성 시 시작 시간이 양조장 운영시간 밖이면 요청을 반려한다")
void create_joy_rejects_start_time_outside_brewery_open_time() {
    Long userId = 1L;
    Brewery brewery = brewery();
    ReflectionTestUtils.setField(brewery, "id", 5L);
    ReqJoyDto dto = reqJoyDto();
    dto.setSchedules(List.of(schedule(DayOfWeek.Mon, LocalTime.of(18, 30))));
    given(breweryRepository.findActiveByUserId(userId)).willReturn(Optional.of(brewery));
    givenOpenTimes(
            5L,
            LocalDate.now(),
            openTime(DayOfWeek.Mon, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(18, 0))
    );

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyService.createJoy(userId, dto)
    );

    assertEquals(ApplicationError.INVALID_TIME, exception.getApplicationError());
    verify(joyRepository, never()).save(any(Joy.class));
}
```

- [ ] **Step 1.6: 체험 일정 변경이 운영시간 밖 시작 시간을 저장하지 않는 실패 테스트를 추가한다**

`JoyServiceTest`에 아래 테스트를 추가한다. 현재 구현은 운영시간 검증이 없어 같은 적용일 스냅샷 삭제까지 진행하므로 실패해야 한다.

```java
@Test
@DisplayName("체험 일정 변경 시 시작 시간이 양조장 운영시간 밖이면 요청을 반려한다")
void update_joy_schedule_rejects_start_time_outside_brewery_open_time() {
    Long userId = 1L;
    Long joyId = 10L;
    LocalDate effectiveDate = LocalDate.now().plusDays(1);
    Brewery brewery = brewery();
    ReflectionTestUtils.setField(brewery, "id", 5L);
    Joy joy = joy(brewery);
    ReqUpdateJoyScheduleDto dto = reqUpdateJoyScheduleDto(joyId, effectiveDate);
    dto.setSchedules(List.of(schedule(DayOfWeek.Mon, LocalTime.of(18, 30))));
    given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
    given(joyRepository.findActiveByBreweryIdAndJoyIdIncludingDeletedBrewery(5L, joyId)).willReturn(Optional.of(joy));
    givenOpenTimes(
            5L,
            effectiveDate,
            openTime(DayOfWeek.Mon, effectiveDate, LocalTime.of(9, 0), LocalTime.of(18, 0))
    );

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyService.updateJoySchedule(userId, dto)
    );

    assertEquals(ApplicationError.INVALID_TIME, exception.getApplicationError());
    verify(joyWeeklyStartTimeRepository, never()).deleteByJoyIdAndEffectiveDate(joyId, effectiveDate);
    verify(joyOrderService, never()).setRefundRequestedByJoyScheduleChange(joyId, effectiveDate);
}
```

- [ ] **Step 1.7: 최신 운영시간 버전에 없는 요일을 저장하지 않는 실패 테스트를 추가한다**

`JoyServiceTest`에 아래 테스트를 추가한다. 최신 주간 운영시간 버전에 월요일 row가 없으면 과거 월요일 운영시간을 되살리지 않고 거부해야 한다.

```java
@Test
@DisplayName("체험 일정 변경 시 최신 양조장 주간 버전에 없는 요일이면 요청을 반려한다")
void update_joy_schedule_rejects_day_missing_from_latest_brewery_open_time_version() {
    Long userId = 1L;
    Long joyId = 10L;
    LocalDate effectiveDate = LocalDate.now().plusDays(1);
    Brewery brewery = brewery();
    ReflectionTestUtils.setField(brewery, "id", 5L);
    Joy joy = joy(brewery);
    ReqUpdateJoyScheduleDto dto = reqUpdateJoyScheduleDto(joyId, effectiveDate);
    dto.setSchedules(List.of(schedule(DayOfWeek.Mon, LocalTime.of(10, 0))));
    given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
    given(joyRepository.findActiveByBreweryIdAndJoyIdIncludingDeletedBrewery(5L, joyId)).willReturn(Optional.of(joy));
    givenOpenTimes(
            5L,
            effectiveDate,
            openTime(DayOfWeek.Tue, effectiveDate, LocalTime.of(9, 0), LocalTime.of(18, 0))
    );

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyService.updateJoySchedule(userId, dto)
    );

    assertEquals(ApplicationError.INVALID_TIME, exception.getApplicationError());
    verify(joyWeeklyStartTimeRepository, never()).deleteByJoyIdAndEffectiveDate(joyId, effectiveDate);
}
```

- [ ] **Step 1.8: 실패 확인**

Run:

```bash
./gradlew test --tests '*JoyServiceTest'
```

Expected:

```text
create_joy_rejects_start_time_outside_brewery_open_time 실패
update_joy_schedule_rejects_start_time_outside_brewery_open_time 실패
update_joy_schedule_rejects_day_missing_from_latest_brewery_open_time_version 실패
```

---

### Task 2: `JoyService` 저장 전 운영시간 검증 구현

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
- Test: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`

- [ ] **Step 2.1: 운영시간 repository 의존성을 추가한다**

`JoyService` import에 아래 항목을 추가한다.

```java
import com.example.monghyang.domain.brewery.entity.BreweryWeeklyOpenTime;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
```

`JoyService` 필드에 아래 의존성을 추가한다.

```java
private final BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
```

- [ ] **Step 2.2: 체험 생성 저장 전에 운영시간 검증을 호출한다**

`createJoy(...)`의 brewery 조회 직후 적용일을 한 번만 계산하고, 검증과 스냅샷 저장이 같은 날짜 기준을 쓰도록 아래처럼 변경한다.

```java
Brewery brewery = breweryRepository.findActiveByUserId(userId).orElseThrow(() ->
        new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
LocalDate effectiveDate = LocalDate.now();
validateWithinOpenTimes(brewery, reqJoyDto.getSchedules(), effectiveDate, reqJoyDto.getTime_unit());
validateNotOverlappingBreakTimes(brewery, reqJoyDto.getSchedules(), effectiveDate, reqJoyDto.getTime_unit());
```

같은 메서드 하단의 최초 스냅샷 저장도 동일한 `effectiveDate`를 사용한다.

```java
saveWeeklyStartTimes(joy, reqJoyDto.getSchedules(), effectiveDate);
```

- [ ] **Step 2.3: 체험 일정 변경 저장 전에 운영시간 검증을 호출한다**

`updateJoySchedule(...)`의 중복 검증 직후 검증 순서를 아래처럼 변경한다.

```java
validateScheduleDuplicates(dto.getSchedules());
validateWithinOpenTimes(brewery, dto.getSchedules(), dto.getEffective_date(), joy.getTimeUnit());
validateNotOverlappingBreakTimes(brewery, dto.getSchedules(), dto.getEffective_date(), joy.getTimeUnit());
```

- [ ] **Step 2.4: 최신 주간 운영시간 선택 helper를 추가한다**

`JoyService`의 private helper 영역에 아래 메서드를 추가한다.

```java
/**
 * 적용일 기준 최신 양조장 주간 운영시간 버전 안에서 요청 요일의 운영시간을 찾습니다.
 *
 * @param openTimes     적용일 계산에 필요한 양조장 운영시간 스냅샷 목록
 * @param effectiveDate 체험 일정 적용 시작일
 * @param dayOfWeek     요청 요일
 * @return 최신 주간 버전 안의 요청 요일 운영시간. 없으면 null
 */
private BreweryWeeklyOpenTime findActiveOpenTime(List<BreweryWeeklyOpenTime> openTimes, LocalDate effectiveDate, DayOfWeek dayOfWeek) {
    LocalDate latestEffectiveDate = openTimes.stream()
            .filter(openTime -> !openTime.getEffectiveDate().isAfter(effectiveDate))
            .map(BreweryWeeklyOpenTime::getEffectiveDate)
            .max(LocalDate::compareTo)
            .orElse(null);
    if (latestEffectiveDate == null) {
        return null;
    }
    return openTimes.stream()
            .filter(openTime -> openTime.getEffectiveDate().equals(latestEffectiveDate) && openTime.getDayOfWeek() == dayOfWeek)
            .findFirst()
            .orElse(null);
}
```

- [ ] **Step 2.5: 운영시간 범위 검증 helper를 추가한다**

`JoyService`의 private helper 영역에 아래 메서드를 추가한다.

```java
/**
 * 체험 시작 시간 요청이 적용일 기준 양조장 운영시간 안에 들어오는지 검증합니다.
 *
 * @param brewery       체험이 속한 양조장
 * @param schedules     요청된 요일별 체험 시작 시간
 * @param effectiveDate 체험 일정 적용 시작일
 * @param timeUnit      체험 진행 시간 단위
 */
private void validateWithinOpenTimes(Brewery brewery, List<JoyScheduleDto> schedules, LocalDate effectiveDate, Integer timeUnit) {
    List<BreweryWeeklyOpenTime> openTimes = breweryWeeklyOpenTimeRepository.findActiveAndFutureOpenTimesInMonth(
            brewery.getId(),
            effectiveDate,
            effectiveDate.plusDays(1)
    );
    for (JoyScheduleDto schedule : schedules) {
        // 최신 주간 운영시간 버전에 요청 요일이 없으면 저장할 수 없는 시작 시간으로 판단한다.
        BreweryWeeklyOpenTime activeOpenTime = findActiveOpenTime(openTimes, effectiveDate, schedule.getDay_of_week());
        if (activeOpenTime == null) {
            throw new ApplicationException(ApplicationError.INVALID_TIME);
        }
        for (LocalTime startTime : schedule.getStart_times()) {
            // 체험 종료 시간이 운영 종료 시간보다 늦어지는지 함께 검증한다.
            LocalTime endTime = startTime.plusMinutes(timeUnit);
            boolean outsideOpenTime = startTime.isBefore(activeOpenTime.getOpenTime())
                    || endTime.isAfter(activeOpenTime.getCloseTime());
            if (outsideOpenTime) {
                throw new ApplicationException(ApplicationError.INVALID_TIME);
            }
        }
    }
}
```

- [ ] **Step 2.6: `JoyServiceTest` 통과 확인**

Run:

```bash
./gradlew test --tests '*JoyServiceTest'
```

Expected:

```text
BUILD SUCCESSFUL
```

---

### Task 3: 관련 조회/예약 테스트로 비대칭 해소 검증

**Files:**
- Verify: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
- Verify: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
- Verify: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`

- [ ] **Step 3.1: 체험 저장, 캘린더 조회, 예약 생성 테스트를 함께 실행한다**

Run:

```bash
./gradlew test --tests '*JoyServiceTest' --tests '*JoySlotServiceTest' --tests '*JoyOrderServiceTest'
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3.2: 전체 테스트를 실행한다**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

---

### Task 4: 자체 검토와 완료 보고

**Files:**
- Review: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
- Review: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`

- [ ] **Step 4.1: 범위 검토**

확인할 항목:

```text
새 dependency, 새 DB migration, 새 DTO, 새 controller 변경이 없는지 확인한다.
운영시간 밖 스냅샷 저장만 차단하고 기존 휴게시간 충돌 검증은 유지했는지 확인한다.
기존 조회/예약 API의 조건과 저장 API의 조건이 같은지 확인한다.
```

- [ ] **Step 4.2: 단순성 검토**

확인할 항목:

```text
검증을 위해 새 서비스나 공용 유틸을 만들지 않았는지 확인한다.
`JoySlotService`와 중복되는 helper가 생겼더라도 현재 범위에서는 공유 추상화를 만들지 않는다.
테스트 편의를 위해 production code의 캡슐화나 생성자를 변경하지 않았는지 확인한다.
```

- [ ] **Step 4.3: 완료 보고 작성**

완료 보고에는 아래 내용을 포함한다.

```text
변경 파일:
- src/main/java/com/example/monghyang/domain/joy/service/JoyService.java
- src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java

구현 동작:
- 체험 생성 저장 전 운영시간 범위 검증
- 체험 일정 변경 저장 전 운영시간 범위 검증
- 운영시간이 없는 요일 저장 거부
- 운영 시작/운영 종료 경계 조건은 조회/예약 API와 같은 기준으로 처리

검증:
- ./gradlew test --tests '*JoyServiceTest'
- ./gradlew test --tests '*JoySlotServiceTest' --tests '*JoyOrderServiceTest'
- ./gradlew test

알려진 범위 밖 항목:
- 이미 저장된 잘못된 체험 시작 시간 스냅샷 데이터 정리
```
