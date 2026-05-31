# 양조장 소프트 딜리트 하위 체험 예약 차단 구현 계획

> **에이전트 작업자 필수 하위 스킬:** 이 계획을 실행할 때는 `superpowers:executing-plans`를 기본으로 사용한다. 양조장 삭제 상태가 체험 생성, 일정 변경, 예약 가능 시간 조회, 예약 생성/변경 경로에 함께 걸쳐 있으므로 작업은 체크박스(`- [ ]`)로 추적한다. 실행 전 현재 브랜치가 `main` 또는 `master`이면 `superpowers:using-git-worktrees`를 먼저 적용한다.

**목표:** `Brewery.isDeleted = true`인 양조장 소속 체험이 스냅샷 일정 데이터가 남아 있더라도 신규 체험 관리, 예약 가능 조회, 예약 생성/변경 경로에 사용되지 않도록 차단한다.

**아키텍처:** 하위 `Joy`와 `JoyWeeklyStartTime` 스냅샷은 물리 삭제하거나 일괄 소프트 딜리트하지 않고 보존한다. 대신 active 양조장 조건을 `BreweryRepository`, `JoyRepository`, 예약 운영시간 조회 쿼리의 경계에 강제해 삭제된 양조장의 하위 스케줄이 비즈니스 흐름으로 다시 진입하지 못하게 한다. 복구 가능성을 해치지 않기 위해 `breweryRestore`, 사용자 개인정보 조회, 기존 예약 이력 정리처럼 삭제 상태 자체를 보여 주거나 복구해야 하는 경로는 기존 무필터 조회를 유지한다.

**기술 스택:** Java 21, Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5.12.2, Mockito Core 5.17.0, Mockito JUnit Jupiter 5.14.2. 정확한 버전은 `docs/context7-dependencies.yaml` 기준이다.

**상태:** 작성 완료 / 미승인

---

## 기준 문서 및 규칙

- 기준 보고서: `docs/plan/snapshot_conflict_analysis_report.md`의 `[결함 3]`
- 요청 파일명 `snapshot_confilct_analysis_report.md`는 저장소에서 발견되지 않았고, 실제 보고서 경로는 `docs/plan/snapshot_conflict_analysis_report.md`로 확인했다.
- `superpowers:writing-plans`의 영문 헤더 예시는 `AGENTS.md`의 한국어 문서 작성 규칙과 충돌하므로, 더 높은 우선순위인 `AGENTS.md` §0.1 및 §13을 적용해 본 계획서를 한국어로 작성한다.
- 테스트 작성 기준: `docs/junit-unit-test-guide.md`

## 승인 및 실행 경계

- 이 문서는 계획이며, 사용자가 명시적으로 승인하기 전에는 프로덕션 코드와 테스트 코드를 수정하지 않는다.
- 구현 승인 후에는 현재 작업 트리를 확인하고, 이번 계획 범위 파일만 수정한다.
- 코드 변경 커밋은 별도 사용자 확인 전에는 만들지 않는다.
- 양조장 탈퇴 시 기존 미래 예약을 자동 환불 요청으로 전환하는 정책은 이번 계획에 포함하지 않는다. 이 계획의 범위는 `[결함 3]` 보고서가 지적한 삭제 양조장 소속 체험의 신규 예약/조회/관리 진입 차단이다.
- `JoyWeeklyStartTime` 스냅샷은 삭제하지 않는다. 양조장 복구 후 기존 스냅샷을 다시 사용할 수 있어야 하므로, 차단은 active 조회 조건으로 처리한다.

## 의존성 및 Context7 기준

- version: Spring Boot `3.5.3`, Spring Data JPA `3.5.1`, JUnit `5.12.2`, Mockito Core `5.17.0`, Mockito JUnit Jupiter `5.14.2`
- source: `docs/context7-dependencies.yaml`
- context7_library_id: `not_used`
- reason: 기존 저장소의 `@Query`, `JpaRepository`, JUnit 5, Mockito 패턴을 확장하는 계획이며, 새로운 API signature나 버전별 설정 옵션 확인이 필요하지 않다.

## 파일 구조

- 수정: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryRepository.java`
  - active 양조장 owner 조회 메서드와 예약 운영시간 조회의 삭제 양조장 필터를 추가한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyRepository.java`
  - active/deleted 체험 조회 쿼리에 소속 양조장의 active 조건을 함께 적용한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
  - 체험 생성, 수정, 삭제, 복구, 매진 처리, 일정 변경에서 active 양조장만 대상으로 삼는다.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`
  - 별도 휴무일, 운영/휴게시간 스케줄 변경은 active 양조장만 대상으로 삼고, 탈퇴/복구 경로는 삭제 상태 판단을 위해 기존 무필터 조회를 유지한다.
- 테스트 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
  - active 양조장 조회 메서드 사용을 검증하고, 탈퇴 양조장 소속 체험 생성/일정 변경이 차단되는지 검증한다.
- 테스트 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
  - 예약 운영시간 조회가 삭제 양조장을 거부할 때 슬롯 증가가 일어나지 않는지 검증한다.
- 테스트 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
  - 삭제 양조장 소속 체험이 active 체험 조회에서 제외된다는 서비스 계약을 명시한다.
- 테스트 추가: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`
  - 탈퇴 양조장은 운영/휴게시간 스케줄 변경을 할 수 없음을 검증한다.

---

### 작업 0: 실행 전 저장소 규칙 확인

**파일:**
- 읽기: `docs/context7-dependencies.yaml`
- 읽기: `docs/junit-unit-test-guide.md`
- 확인: `git status`

- [ ] **단계 0.1: 의존성 및 테스트 기준 문서 확인**

실행:

```bash
sed -n '1,520p' docs/context7-dependencies.yaml
sed -n '1,220p' docs/junit-unit-test-guide.md
```

예상:

```text
Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5.12.2, Mockito 관련 버전을 확인한다.
단위 테스트는 Spring Context 없이 MockitoExtension 기반으로 작성한다.
```

- [ ] **단계 0.2: 현재 브랜치와 작업 트리 확인**

실행:

```bash
git status --short --branch
```

예상:

```text
현재 브랜치가 main 또는 master이면 구현을 시작하지 않는다.
사용자 변경이 있으면 보존하고, 이번 계획 범위 파일만 수정한다.
```

---

### 작업 1: 삭제 양조장 차단 테스트 추가

**파일:**
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
- 추가: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`

- [ ] **단계 1.1: `JoyServiceTest`의 양조장 조회 스텁을 active 조회로 교체**

`JoyService`의 체험 관리 경로는 active 양조장만 사용해야 하므로 기존 테스트의 다음 스텁을 교체한다.

변경 전:

```java
given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
```

변경 후:

```java
given(breweryRepository.findActiveByUserId(userId)).willReturn(Optional.of(brewery));
```

적용 대상 테스트:

```text
delete_joy_rejects_already_deleted_joy
restore_joy_uses_deleted_joy_lookup
create_joy_saves_initial_weekly_start_time_snapshot
update_joy_schedule_replaces_snapshot_and_requests_refund
update_joy_schedule_rejects_duplicate_day_of_week
create_joy_rejects_start_time_overlapping_break_time
update_joy_schedule_rejects_start_time_overlapping_break_time
```

- [ ] **단계 1.2: `JoyServiceTest`에 탈퇴 양조장 체험 생성 차단 테스트 추가**

다음 테스트를 `JoyServiceTest`에 추가한다.

```java
@Test
@DisplayName("탈퇴한 양조장은 체험을 새로 등록할 수 없다")
void create_joy_rejects_deleted_brewery() {
    Long userId = 1L;
    ReqJoyDto dto = reqJoyDto();
    given(breweryRepository.findActiveByUserId(userId)).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyService.createJoy(userId, dto)
    );

    assertEquals(ApplicationError.BREWERY_NOT_FOUND, exception.getApplicationError());
}
```

- [ ] **단계 1.3: `JoyServiceTest`에 탈퇴 양조장 체험 일정 변경 차단 테스트 추가**

다음 테스트를 `JoyServiceTest`에 추가한다.

```java
@Test
@DisplayName("탈퇴한 양조장은 체험 시작 시간 스냅샷을 변경할 수 없다")
void update_joy_schedule_rejects_deleted_brewery() {
    Long userId = 1L;
    Long joyId = 10L;
    ReqUpdateJoyScheduleDto dto = reqUpdateJoyScheduleDto(joyId, LocalDate.now().plusDays(1));
    given(breweryRepository.findActiveByUserId(userId)).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyService.updateJoySchedule(userId, dto)
    );

    assertEquals(ApplicationError.BREWERY_NOT_FOUND, exception.getApplicationError());
}
```

- [ ] **단계 1.4: `JoyOrderServiceTest`에 예약 운영시간 조회 차단 테스트 추가**

다음 테스트를 `JoyOrderServiceTest`에 추가한다.

```java
@Test
@DisplayName("예약 슬롯 증가는 탈퇴한 양조장 소속 체험이면 슬롯을 증가시키지 않는다")
void reservation_joy_slot_count_rejects_deleted_brewery_joy() {
    Long joyId = 10L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);
    given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(mock(Joy.class)));
    given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
            .willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.reservationJoySlotCount(joyId, reservationDate, reservationTime, 2)
    );

    assertEquals(ApplicationError.BREWERY_NOT_FOUND, exception.getApplicationError());
    verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
}
```

- [ ] **단계 1.5: `JoySlotServiceTest`에 삭제 양조장 소속 체험 조회 차단 계약 테스트 추가**

다음 테스트를 `JoySlotServiceTest`에 추가한다.

```java
@Test
@DisplayName("탈퇴한 양조장 소속 체험의 예약 가능 날짜 조회는 JOY_NOT_FOUND로 거부한다")
void get_impossible_date_rejects_deleted_brewery_joy() {
    Long joyId = 10L;
    ReqFindJoySlotDateDto dto = new ReqFindJoySlotDateDto();
    dto.setJoyId(joyId);
    dto.setYear(2026);
    dto.setMonth(6);
    given(joyRepository.findActiveById(joyId)).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joySlotService.getImpossibleDate(dto)
    );

    assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
    verify(breweryWeeklyOpenTimeRepository, never()).findActiveAndFutureOpenTimesInMonth(any(), any(), any());
}
```

- [ ] **단계 1.6: `BreweryServiceTest`를 새 위치에 추가**

다음 파일을 생성한다. 기존 `src/test/java/com/example/monghyang/devtest/BreweryServiceTest.java`는 건드리지 않는다.

```java
package com.example.monghyang.domain.brewery.service;

import com.example.monghyang.domain.auth.dto.BreweryScheduleDto;
import com.example.monghyang.domain.batch.service.JoyOrderBatchService;
import com.example.monghyang.domain.brewery.dto.ReqUpdateBreweryScheduleDto;
import com.example.monghyang.domain.brewery.repository.BreweryClosedDateRepository;
import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.brewery.tag.BreweryTagRepository;
import com.example.monghyang.domain.global.DayOfWeek;
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.repository.JoyStatusHistoryRepository;
import com.example.monghyang.domain.joy.service.JoyOrderService;
import com.example.monghyang.domain.product.service.ProductService;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BreweryServiceTest {
    @Mock BreweryRepository breweryRepository;
    @Mock UsersRepository usersRepository;
    @Mock BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock BreweryImageRepository breweryImageRepository;
    @Mock StorageService storageService;
    @Mock BreweryTagRepository breweryTagRepository;
    @Mock JoyRepository joyRepository;
    @Mock ProductService productService;
    @Mock RegionTypeRepository regionTypeRepository;
    @Mock BreweryClosedDateRepository breweryClosedDateRepository;
    @Mock JoyOrderRepository joyOrderRepository;
    @Mock JoyStatusHistoryRepository joyStatusHistoryRepository;
    @Mock JoyOrderService joyOrderService;
    @Mock JoyOrderBatchService joyOrderBatchService;
    @Mock BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    @Mock BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @InjectMocks BreweryService breweryService;

    @Test
    @DisplayName("탈퇴한 양조장은 운영 시간 스냅샷을 변경할 수 없다")
    void update_brewery_schedule_rejects_deleted_brewery() {
        Long userId = 1L;
        ReqUpdateBreweryScheduleDto dto = updateScheduleDto();
        given(breweryRepository.findActiveByUserId(userId)).willReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> breweryService.updateBrewerySchedule(userId, dto)
        );

        assertEquals(ApplicationError.BREWERY_NOT_FOUND, exception.getApplicationError());
        verify(breweryWeeklyOpenTimeRepository, never()).deleteByBreweryIdAndEffectiveDate(any(), any());
    }

    private ReqUpdateBreweryScheduleDto updateScheduleDto() {
        BreweryScheduleDto schedule = new BreweryScheduleDto();
        schedule.setDay_of_week(DayOfWeek.Mon);
        schedule.setOpen_time(LocalTime.of(9, 0));
        schedule.setClose_time(LocalTime.of(18, 0));

        ReqUpdateBreweryScheduleDto dto = new ReqUpdateBreweryScheduleDto();
        dto.setEffective_date(LocalDate.now().plusDays(1));
        dto.setSchedules(List.of(schedule));
        return dto;
    }
}
```

- [ ] **단계 1.7: 테스트 실패 확인**

실행:

```bash
./gradlew test --tests '*JoyServiceTest' --tests '*JoyOrderServiceTest' --tests '*JoySlotServiceTest' --tests '*BreweryServiceTest'
```

예상:

```text
컴파일 실패: BreweryRepository.findActiveByUserId 메서드가 존재하지 않는다.
또는 기존 JoyServiceTest 스텁이 실제 서비스 호출 메서드와 맞지 않아 실패한다.
```

---

### 작업 2: `BreweryRepository` active 양조장 조회 계약 추가

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/brewery/repository/BreweryRepository.java`

- [ ] **단계 2.1: owner 기준 active 양조장 조회 메서드 추가**

`findByUserId` 아래에 다음 메서드를 추가한다. 기존 `findByUserId`는 삭제 상태 확인, 복구, 개인정보 조회에서 필요하므로 유지한다.

```java
/**
 * 탈퇴하지 않은 양조장을 회원 식별자로 조회합니다.
 *
 * @param userId 회원 식별자
 * @return 탈퇴하지 않은 양조장
 */
@Query("select b from Brewery b where b.user.id = :userId and b.isDeleted = false")
Optional<Brewery> findActiveByUserId(@Param("userId") Long userId);
```

- [ ] **단계 2.2: 예약 운영시간 조회 쿼리에 삭제 필터 추가**

`findJoyTimeInfoByJoyId`의 `where` 절을 다음처럼 바꾼다.

```java
where j.id = :joyId
  and j.isDeleted = false
  and b.isDeleted = false
  and wot.dayOfWeek = :dayOfWeek
  and wot.effectiveDate = (
      select max(wot2.effectiveDate)
      from BreweryWeeklyOpenTime wot2
      where wot2.brewery = b
        and wot2.dayOfWeek = :dayOfWeek
        and wot2.effectiveDate <= :reservationDate
  )
```

Javadocs의 반환 설명을 다음 문장으로 보강한다.

```java
 * 체험 또는 소속 양조장이 삭제 상태이면 Optional.empty()를 반환합니다.
```

- [ ] **단계 2.3: 컴파일 확인**

실행:

```bash
./gradlew compileJava
```

예상:

```text
BUILD SUCCESSFUL
```

---

### 작업 3: `JoyRepository` active 체험 조회에 active 양조장 조건 추가

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyRepository.java`

- [ ] **단계 3.1: `findActiveById` 쿼리 변경**

변경 후:

```java
@Query("""
    select j from Joy j
    join j.brewery b
    where j.id = :joyId
      and j.isDeleted = false
      and b.isDeleted = false
""")
Optional<Joy> findActiveById(@Param("joyId") Long joyId);
```

- [ ] **단계 3.2: `findActiveByBreweryId` 쿼리 변경**

변경 후:

```java
@Query("""
    select j from Joy j
    join j.brewery b
    where b.id = :breweryId
      and j.isDeleted = false
      and b.isDeleted = false
""")
List<Joy> findActiveByBreweryId(@Param("breweryId") Long breweryId);
```

- [ ] **단계 3.3: `findActiveByUserId` 쿼리 변경**

변경 후:

```java
@Query("""
    select j from Joy j
    join j.brewery b
    where b.user.id = :userId
      and j.isDeleted = false
      and b.isDeleted = false
""")
List<Joy> findActiveByUserId(@Param("userId") Long userId);
```

- [ ] **단계 3.4: `findActiveByBreweryIdAndJoyId` 쿼리 변경**

변경 후:

```java
@Query("""
    select j from Joy j
    join j.brewery b
    where j.id = :joyId
      and b.id = :breweryId
      and j.isDeleted = false
      and b.isDeleted = false
""")
Optional<Joy> findActiveByBreweryIdAndJoyId(@Param("breweryId") Long breweryId, @Param("joyId") Long joyId);
```

- [ ] **단계 3.5: `findDeletedByBreweryIdAndJoyId` 쿼리 변경**

삭제된 체험 복구도 active 양조장 아래에서만 허용한다.

```java
@Query("""
    select j from Joy j
    join j.brewery b
    where j.id = :joyId
      and b.id = :breweryId
      and j.isDeleted = true
      and b.isDeleted = false
""")
Optional<Joy> findDeletedByBreweryIdAndJoyId(@Param("breweryId") Long breweryId, @Param("joyId") Long joyId);
```

- [ ] **단계 3.6: Javadocs 의미 보강**

각 active 조회 Javadocs에 다음 의미를 반영한다.

```java
 * 체험과 소속 양조장이 모두 삭제되지 않은 경우만 조회합니다.
```

`findIdByBreweryId` Javadocs는 그대로 둔다. 양조장 일정 변경 환불 대상 선정은 삭제 여부와 무관하게 기존 예약을 다뤄야 하기 때문이다.

---

### 작업 4: 서비스 계층에서 active 양조장 조회 사용

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
- 수정: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`

- [ ] **단계 4.1: `JoyService` 체험 관리 경로의 양조장 조회 교체**

다음 메서드의 첫 양조장 조회를 `findActiveByUserId`로 교체한다.

```text
createJoy
updateJoySchedule
deleteJoy
restoreJoy
setSoldout
unSetSoldout
updateJoy
```

변경 전:

```java
Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
        new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
```

변경 후:

```java
Brewery brewery = breweryRepository.findActiveByUserId(userId).orElseThrow(() ->
        new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
```

- [ ] **단계 4.2: `BreweryService` active 상태가 필요한 owner 작업의 조회 교체**

다음 메서드는 탈퇴한 양조장에 대해 수행되면 안 되므로 active 조회로 교체한다.

```text
deleteClosedDate
addClosedDateTry
addClosedDateConfirmed
updateBrewerySchedule
```

변경 후 공통 형태:

```java
Brewery brewery = breweryRepository.findActiveByUserId(userId).orElseThrow(() ->
        new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
```

- [ ] **단계 4.3: 탈퇴/복구 경로는 무필터 조회 유지**

다음 메서드는 삭제 상태 자체를 변경해야 하므로 `findByUserId`를 유지한다.

```text
breweryQuit
breweryRestore
```

유지할 코드:

```java
Brewery brewery = breweryRepository.findByUserId(userId).orElseThrow(() ->
        new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
```

- [ ] **단계 4.4: 컴파일 확인**

실행:

```bash
./gradlew compileJava
```

예상:

```text
BUILD SUCCESSFUL
```

---

### 작업 5: 테스트 정리 및 검증

**파일:**
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
- 추가: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`

- [ ] **단계 5.1: 관련 테스트 실행**

실행:

```bash
./gradlew test --tests '*JoyServiceTest' --tests '*JoyOrderServiceTest' --tests '*JoySlotServiceTest' --tests '*BreweryServiceTest'
```

예상:

```text
BUILD SUCCESSFUL
```

- [ ] **단계 5.2: 전체 테스트 실행**

실행:

```bash
./gradlew test
```

예상:

```text
BUILD SUCCESSFUL
```

- [ ] **단계 5.3: 정적 확인**

실행:

```bash
rg -n "findByUserId\\(userId\\)|findByUserId\\(users.getId\\(\\)\\)|findActiveByUserId|findJoyTimeInfoByJoyId|b.isDeleted = false|j.isDeleted = false" src/main/java/com/example/monghyang/domain
```

예상:

```text
JoyService의 체험 관리 메서드는 findActiveByUserId를 사용한다.
BreweryService의 휴무일, 스케줄 변경 메서드는 findActiveByUserId를 사용한다.
breweryQuit, breweryRestore, UsersService의 개인정보 조회/회원 탈퇴 경로는 findByUserId를 유지한다.
findJoyTimeInfoByJoyId 쿼리에는 j.isDeleted = false와 b.isDeleted = false가 모두 존재한다.
```

---

## 자체 검토 체크리스트

- `[결함 3]`의 핵심 경로인 예약 가능 날짜 조회, 남은 자리 조회, 예약 슬롯 증가, 예약 사전등록, 예약 변경이 active 양조장 조건 아래로 들어간다.
- 하위 체험과 스냅샷을 삭제하지 않으므로 양조장 복구 시 기존 스케줄 복구 가능성이 유지된다.
- `findIdByBreweryId`, `findTimeUnitByJoyId`처럼 기존 예약 환불/이력 정리에 필요한 삭제 무관 조회는 건드리지 않는다.
- 새 의존성, 새 추상화, 전역 소프트 딜리트 프레임워크는 추가하지 않는다.
- 테스트 편의를 위해 프로덕션 코드의 가시성, 생성자, 필드를 바꾸지 않는다.
- 구현 후에는 `superpowers:verification-before-completion`을 사용해 검증 결과를 확인한 뒤 완료를 보고한다.
