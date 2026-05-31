# 체험 소프트 딜리트 예약/조회 충돌 개선 구현 계획

> **에이전트 작업자 필수 하위 스킬:** 이 계획을 실행할 때는 `superpowers:subagent-driven-development`를 기본으로 사용한다. 체험 삭제 상태가 예약 생성, 예약 변경, 캘린더 조회, 관리자 조회에 걸쳐 데이터 정합성에 영향을 주므로 각 작업은 체크박스(`- [ ]`)로 추적하고, 작업 단위 구현 후 검토를 거친다.

**목표:** `Joy.isDeleted = true`인 체험이 관리자 체험 목록, 예약 가능 날짜/시간 조회, 예약 생성/변경 경로에 다시 노출되거나 신규 예약에 사용되지 않도록 차단한다.

**아키텍처:** `JoyWeeklyStartTime` 스냅샷은 복구 가능성을 위해 물리 삭제하지 않고 유지한다. 대신 비즈니스 진입점에서 `JoyRepository`의 active/deleted 조회 메서드를 명시적으로 사용해 삭제된 체험을 예약/조회 흐름에서 격리한다. 새 계층이나 공통 소프트 딜리트 추상화는 만들지 않고, 현재 `JoyService`, `JoySlotService`, `JoyOrderService` 흐름에 필요한 최소 조회 조건만 추가한다.

**기술 스택:** Java 21, Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5.12.2, Mockito 5.17.0. 정확한 라이브러리 버전은 `docs/context7-dependencies.yaml` 기준이다.

**상태:** 작성 완료 / 미승인

---

## 승인 및 실행 경계

- 이 문서는 계획이며, 사용자가 명시적으로 승인하기 전에는 프로덕션 코드와 테스트 코드를 수정하지 않는다.
- 구현 승인 후에는 `superpowers:using-git-worktrees`를 먼저 적용한다. 현재 브랜치가 `main` 또는 `master`이면 `codex/` prefix 브랜치 또는 별도 worktree를 사용한다.
- 코드 변경 커밋은 별도 사용자 확인 전에는 만들지 않는다.
- `JoyWeeklyStartTime` 잔존 스냅샷은 이번 작업에서 삭제하지 않는다. 삭제된 체험 복구 시 기존 스냅샷을 재사용할 수 있어야 하므로, 격리는 조회 조건으로 처리한다.
- 양조장 탈퇴 상태(`Brewery.isDeleted = true`) 차단은 `[결함 3]` 범위이므로 이 계획에 포함하지 않는다.
- `JoyReviewService`의 삭제된 체험 리뷰 작성 가능성은 예약/조회 충돌의 직접 경로가 아니므로 이 계획에 포함하지 않는다.

## 의존성 및 Context7 기준

- version: Spring Boot `3.5.3`, Spring Data JPA `3.5.1`, JUnit `5.12.2`, Mockito `5.17.0`
- source: `docs/context7-dependencies.yaml`
- context7_library_id: `not_used`
- reason: 기존 저장소의 `@Query` 패턴과 Mockito/JUnit 단위 테스트 패턴을 그대로 확장하는 계획이며, 새로운 API signature나 버전별 설정 옵션 확인이 필요하지 않다.

## 파일 구조

- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyRepository.java`
  - active 체험 조회, deleted 체험 복구 조회를 명시하는 메서드를 추가한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
  - 관리자 체험 목록, 체험 수정, 삭제, 품절 처리, 일정 변경은 active 체험만 대상으로 삼고, 복구는 deleted 체험만 대상으로 삼는다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
  - 예약 가능 날짜 조회와 남은 자리 조회에서 deleted 체험을 즉시 `JOY_NOT_FOUND`로 차단한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
  - 예약 슬롯 증가, 예약 생성, 예약 변경에서 deleted 체험을 즉시 `JOY_NOT_FOUND`로 차단한다.
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
  - active 목록 조회, deleted 체험 수정/삭제 차단, deleted 체험 복구 조회를 검증한다.
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
  - deleted 체험의 캘린더 조회와 시간대 조회가 실패하는지 검증한다.
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
  - deleted 체험의 예약 슬롯 증가, 예약 생성, 예약 변경이 실패하는지 검증한다.

---

### 작업 0: 실행 전 저장소 규칙 확인

**파일:**
- 읽기: `docs/context7-dependencies.yaml`
- 읽기: `docs/junit-unit-test-guide.md`
- 확인: `git status`

- [ ] **단계 0.1: 의존성 및 테스트 기준 문서 확인**

실행:

```bash
find docs -maxdepth 4 -type f
```

예상:

```text
docs/context7-dependencies.yaml
docs/junit-unit-test-guide.md
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

- [ ] **단계 0.3: worktree 규칙 적용**

구현 승인 후 `superpowers:using-git-worktrees`를 사용한다. 별도 worktree 생성이 필요하면 `codex/` prefix 브랜치 또는 저장소의 현재 브랜치 정책을 따른다.

---

### 작업 1: `JoyRepository` active/deleted 조회 계약 분리

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyRepository.java`

- [ ] **단계 1.1: repository 메서드 변경 전 컴파일 실패를 만드는 서비스 테스트 작성**

다음 테스트를 `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`에 추가한다:

```java
@Test
@DisplayName("내 체험 목록 조회는 삭제되지 않은 체험만 반환한다")
void get_my_joy_list_returns_active_joys_only() {
    Long userId = 1L;
    Brewery brewery = brewery();
    Joy joy = joy(brewery);
    given(joyRepository.findActiveByUserId(userId)).willReturn(List.of(joy));

    List<ResJoyDto> result = joyService.getMyJoyList(userId);

    assertEquals(1, result.size());
    verify(joyRepository).findActiveByUserId(userId);
}

@Test
@DisplayName("이미 삭제된 체험은 다시 삭제할 수 없다")
void delete_joy_rejects_already_deleted_joy() {
    Long userId = 1L;
    Long joyId = 10L;
    Brewery brewery = brewery();
    ReflectionTestUtils.setField(brewery, "id", 5L);
    given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
    given(joyRepository.findActiveByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyService.deleteJoy(userId, joyId)
    );

    assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
}

@Test
@DisplayName("삭제된 체험 복구는 삭제 상태 체험만 대상으로 한다")
void restore_joy_uses_deleted_joy_lookup() {
    Long userId = 1L;
    Long joyId = 10L;
    Brewery brewery = brewery();
    Joy joy = joy(brewery);
    joy.setDeleted();
    ReflectionTestUtils.setField(brewery, "id", 5L);
    given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));
    given(joyRepository.findDeletedByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.of(joy));

    joyService.restoreJoy(userId, joyId);

    assertEquals(false, joy.getIsDeleted());
    verify(joyRepository).save(joy);
}
```

누락된 import가 있으면 추가한다:

```java
import com.example.monghyang.domain.joy.dto.ResJoyDto;
```

실행:

```bash
./gradlew test --tests '*JoyServiceTest'
```

예상:

```text
컴파일 실패: findActiveByUserId, findActiveByBreweryIdAndJoyId, findDeletedByBreweryIdAndJoyId 메서드가 존재하지 않는다.
```

- [ ] **단계 1.2: `JoyRepository`에 명시적 조회 메서드 추가**

`JoyRepository`의 일반 user/owner 조회 메서드를 active/deleted 의미가 드러나는 메서드로 교체한다:

```java
@Query("select j from Joy j where j.id = :joyId and j.isDeleted = false")
Optional<Joy> findActiveById(@Param("joyId") Long joyId);

@Query("select j from Joy j join j.brewery b where b.user.id = :userId and j.isDeleted = false")
List<Joy> findActiveByUserId(@Param("userId") Long userId);

@Query("select j from Joy j where j.id = :joyId and j.brewery.id = :breweryId and j.isDeleted = false")
Optional<Joy> findActiveByBreweryIdAndJoyId(@Param("breweryId") Long breweryId, @Param("joyId") Long joyId);

@Query("select j from Joy j where j.id = :joyId and j.brewery.id = :breweryId and j.isDeleted = true")
Optional<Joy> findDeletedByBreweryIdAndJoyId(@Param("breweryId") Long breweryId, @Param("joyId") Long joyId);
```

의도적으로 다른 의미를 가진 기존 메서드는 유지한다:

```java
@Query("select j.timeUnit from Joy j where j.id = :joyId")
Optional<Integer> findTimeUnitByJoyId(@Param("joyId") Long joyId);

@Query("select j.id from Joy j where j.brewery.id = :breweryId")
List<Long> findIdByBreweryId(@Param("breweryId") Long breweryId);
```

이유:

```text
findTimeUnitByJoyId는 과거 예약 내역 삭제 조건 판단에 쓰이며, 이미 종료된 예약 히스토리는 체험 삭제 이후에도 정리 가능해야 한다.
findIdByBreweryId는 양조장 일정 변경 환불 대상 선정에 쓰이며, 삭제된 체험에 남아 있는 기존 PAID 예약도 환불 대상에서 누락되면 안 된다.
```

- [ ] **단계 1.3: repository 변경 후 컴파일 확인**

실행:

```bash
./gradlew compileJava compileTestJava
```

예상:

```text
컴파일 실패가 발생하면 남아 있는 findByUserId 또는 findByBreweryIdAndJoyId 호출부를 active/deleted 메서드로 교체한다.
```

---

### 작업 2: `JoyService` 관리자 조회/수정 경로 active 필터 적용

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`

- [ ] **단계 2.1: `JoyService` 조회 메서드 교체**

owner 범위 active 작업의 조회를 교체한다:

```java
Joy joy = joyRepository.findActiveByBreweryIdAndJoyId(brewery.getId(), dto.getJoyId()).orElseThrow(() ->
        new ApplicationException(ApplicationError.JOY_NOT_FOUND));
```

동일한 active 조회를 다음 메서드에 적용한다:

```text
updateJoySchedule
deleteJoy
setSoldout
unSetSoldout
updateJoy
```

`restoreJoy` 조회는 deleted 전용 메서드로 교체한다:

```java
Joy joy = joyRepository.findDeletedByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
        new ApplicationException(ApplicationError.JOY_NOT_FOUND));
joy.unSetDeleted();
joyRepository.save(joy);
```

관리자 체험 목록 조회를 교체한다:

```java
public List<ResJoyDto> getMyJoyList(Long userId) {
    List<Joy> joyList = joyRepository.findActiveByUserId(userId);
    if(joyList.isEmpty()) {
        throw new ApplicationException(ApplicationError.JOY_NOT_FOUND);
    }
    return joyList.stream().map(ResJoyDto::joyFrom).toList();
}
```

- [ ] **단계 2.2: 기존 `JoyServiceTest` stubbing 업데이트**

기존 테스트 stubbing을 교체한다:

```java
given(joyRepository.findByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.of(joy));
```

다음 코드로 바꾼다:

```java
given(joyRepository.findActiveByBreweryIdAndJoyId(5L, joyId)).willReturn(Optional.of(joy));
```

`findDeletedByBreweryIdAndJoyId`는 복구 테스트에서만 사용한다.

- [ ] **단계 2.3: `JoyServiceTest` 통과 확인**

실행:

```bash
./gradlew test --tests '*JoyServiceTest'
```

예상:

```text
JoyServiceTest PASS
```

---

### 작업 3: 예약 캘린더와 시간대 조회에서 deleted 체험 차단

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`

- [ ] **단계 3.1: 실패 테스트 작성**

다음 import를 `JoySlotServiceTest`에 추가한다:

```java
import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
```

다음 테스트를 추가한다:

```java
@Test
@DisplayName("삭제된 체험의 예약 불가 날짜 조회는 JOY_NOT_FOUND로 거부한다")
void get_impossible_date_rejects_deleted_joy() {
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

@Test
@DisplayName("삭제된 체험의 남은 자리 조회는 JOY_NOT_FOUND로 거부한다")
void get_remaining_count_list_rejects_deleted_joy() {
    Long joyId = 10L;
    LocalDate targetDate = LocalDate.of(2026, 6, 1);
    given(joyRepository.findActiveById(joyId)).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joySlotService.getRemainingCountList(joyId, targetDate)
    );

    assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
    verify(joyWeeklyStartTimeRepository, never()).findActiveAndFutureStartTimesInMonth(any(), any(), any());
}
```

실행:

```bash
./gradlew test --tests '*JoySlotServiceTest'
```

예상:

```text
컴파일 실패: findActiveById 메서드 호출부가 아직 구현되지 않았거나 기존 테스트가 findById stubbing을 사용한다.
```

- [ ] **단계 3.2: `JoySlotService` active 조회 적용**

deleted 상태에 민감한 두 조회를 교체한다:

```java
Joy joy = joyRepository.findActiveById(dto.getJoyId()).orElseThrow(() ->
        new ApplicationException(ApplicationError.JOY_NOT_FOUND));
```

and:

```java
Joy joy = joyRepository.findActiveById(joyId).orElseThrow(() ->
        new ApplicationException(ApplicationError.JOY_NOT_FOUND));
```

이 작업에서는 `JoySlotRepository` upsert/decrement SQL을 변경하지 않는다. 슬롯 변경은 작업 4 이후 `JoyOrderService` 검증으로 보호된다.

- [ ] **단계 3.3: 기존 `JoySlotServiceTest` stubbing 업데이트**

다음 코드를:

```java
given(joyRepository.findById(joyId)).willReturn(Optional.of(joy));
```

다음 코드로 바꾼다:

```java
given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(joy));
```

- [ ] **단계 3.4: `JoySlotServiceTest` 통과 확인**

실행:

```bash
./gradlew test --tests '*JoySlotServiceTest'
```

예상:

```text
JoySlotServiceTest PASS
```

---

### 작업 4: 예약 생성/변경 경로에서 deleted 체험 차단

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`

- [ ] **단계 4.1: 실패 테스트 작성**

다음 테스트를 `JoyOrderServiceTest`에 추가한다:

```java
@Test
@DisplayName("예약 슬롯 증가는 삭제된 체험이면 슬롯을 증가시키지 않는다")
void reservation_joy_slot_count_rejects_deleted_joy() {
    Long joyId = 10L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);
    given(joyRepository.findActiveById(joyId)).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.reservationJoySlotCount(joyId, reservationDate, reservationTime, 2)
    );

    assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
    verify(breweryRepository, never()).findJoyTimeInfoByJoyId(any(), any(), any());
    verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
}

@Test
@DisplayName("예약 사전등록은 삭제된 체험이면 주문을 생성하지 않고 예약 슬롯을 롤백한다")
void prepare_order_rejects_deleted_joy_and_rolls_back_slot() {
    Long userId = 1L;
    ReqJoyPreOrderDto dto = preOrderDto(10L);
    given(usersRepository.findById(userId)).willReturn(Optional.of(mock(Users.class)));
    given(joyRepository.findActiveById(dto.getId())).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.prepareOrder(userId, dto)
    );

    assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
    verify(joyOrderRepository, never()).save(any());
    verify(joySlotService).decrementJoySlotCount(
            dto.getId(),
            dto.getReservation_date(),
            dto.getReservation_time(),
            dto.getCount()
    );
}

@Test
@DisplayName("사용자 예약 변경은 삭제된 체험이면 새 슬롯을 증가시키지 않는다")
void update_reservation_rejects_deleted_joy() {
    Long userId = 1L;
    Long joyId = 10L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    ReqUpdateJoyOrderDto dto = updateDto(99L, reservationDate, LocalTime.of(11, 0), 2);
    JoyOrder joyOrder = joyOrder(joyId);
    Users users = mock(Users.class);
    given(users.getId()).willReturn(userId);
    given(joyOrder.getUsers()).willReturn(users);
    given(joyOrder.getReservation()).willReturn(LocalDate.of(2026, 6, 2).atTime(LocalTime.of(10, 0)));
    given(joyOrderRepository.findById(dto.getId())).willReturn(Optional.of(joyOrder));
    given(joyRepository.findActiveById(joyId)).willReturn(Optional.empty());

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.updateReservation(userId, dto)
    );

    assertEquals(ApplicationError.JOY_NOT_FOUND, exception.getApplicationError());
    verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
}
```

다음 helper를 `JoyOrderServiceTest`에 추가한다:

```java
private ReqJoyPreOrderDto preOrderDto(Long joyId) {
    ReqJoyPreOrderDto dto = new ReqJoyPreOrderDto();
    dto.setId(joyId);
    dto.setCount(2);
    dto.setPayer_name("예약자");
    dto.setPayer_phone("01012345678");
    dto.setReservation_date(LocalDate.of(2026, 6, 1));
    dto.setReservation_time(LocalTime.of(10, 0));
    return dto;
}
```

실행:

```bash
./gradlew test --tests '*JoyOrderServiceTest'
```

예상:

```text
컴파일 실패 또는 테스트 실패: JoyOrderService가 아직 findActiveById를 사용하지 않는다.
```

- [ ] **단계 4.2: `JoyOrderService`에 active 체험 확인 메서드 추가**

private helper를 추가한다:

```java
/**
 * 예약 생성과 예약 변경의 대상 체험이 삭제되지 않았는지 확인합니다.
 *
 * @param joyId 체험 식별자
 */
private void verifyActiveJoy(Long joyId) {
    if(joyRepository.findActiveById(joyId).isEmpty()) {
        throw new ApplicationException(ApplicationError.JOY_NOT_FOUND);
    }
}
```

예약 검증 전에 적용한다:

```java
@Transactional
public void reservationJoySlotCount(Long joyId, LocalDate date, LocalTime time, Integer count) {
    verifyActiveJoy(joyId);
    DayOfWeek dayOfWeek = DayOfWeek.from(date.getDayOfWeek());
    JoyInfoDto joyInfoDto = breweryRepository.findJoyTimeInfoByJoyId(joyId, date, dayOfWeek).orElseThrow(() ->
            new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
    verifyReservation(joyId, joyInfoDto, date, time, count);
    joySlotService.reservationJoySlot(joyId, date, time, count, joyInfoDto.maxCount());
}
```

`prepareOrder` 조회를 교체한다:

```java
Joy joy = joyRepository.findActiveById(dto.getId()).orElseThrow(() ->
        new ApplicationException(ApplicationError.JOY_NOT_FOUND));
```

예약 변경에서는 `breweryRepository.findJoyTimeInfoByJoyId`를 호출하기 전에 helper를 적용한다.
`updateReservation`에서는 사용자 소유권 확인과 예약 변경 기한 확인을 먼저 유지한 뒤 active 체험 여부를 확인한다:

```java
Long joyId = joyOrder.getJoy().getId();
if(!joyOrder.getUsers().getId().equals(userId)) {
    throw new ApplicationException(ApplicationError.REQUEST_FORBIDDEN);
}
if(ChronoUnit.DAYS.between(LocalDate.now(), joyOrder.getReservation().toLocalDate()) < 1) {
    throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_UPDATE_ERROR);
}
verifyActiveJoy(joyId);
```

`updateReservationByBrewery`는 `findByIdAndBreweryUserId`가 이미 양조장 소유권을 강제하므로, local `joyId`를 추출한 직후 `verifyActiveJoy(joyId)`를 호출한다.

이후 같은 메서드 안에서 local `joyId`를 `findJoyTimeInfoByJoyId`, `verifyReservation`, `reservationJoySlot`, `decrementJoySlotCount`에 전달해 반복 chained call을 피한다.

- [ ] **단계 4.3: 기존 `JoyOrderServiceTest` stubbing 업데이트**

정상 예약 경로를 검증하는 기존 테스트에는 active 조회 stubbing을 추가한다:

```java
given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(mock(Joy.class)));
```

다음 테스트의 `breweryRepository.findJoyTimeInfoByJoyId(...)` expectation 앞에 stubbing을 추가한다:

```text
reservation_joy_slot_count_rejects_time_not_in_active_snapshot
update_reservation_rejects_time_not_in_active_snapshot
update_reservation_by_brewery_uses_joy_id_and_previous_reservation_for_slots
reservation_joy_slot_count_rejects_break_time_overlap
```

- [ ] **단계 4.4: `JoyOrderServiceTest` 통과 확인**

실행:

```bash
./gradlew test --tests '*JoyOrderServiceTest'
```

예상:

```text
JoyOrderServiceTest PASS
```

---

### 작업 5: 전체 영향 검증 및 자체 검토

**파일:**
- 검증: `src/main/java/com/example/monghyang/domain/joy/repository/JoyRepository.java`
- 검증: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
- 검증: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
- 검증: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- 검증: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
- 검증: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
- 검증: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`

- [ ] **단계 5.1: focused test 실행**

실행:

```bash
./gradlew test --tests '*JoyServiceTest' --tests '*JoySlotServiceTest' --tests '*JoyOrderServiceTest'
```

예상:

```text
JoyServiceTest PASS
JoySlotServiceTest PASS
JoyOrderServiceTest PASS
```

- [ ] **단계 5.2: 컴파일 및 전체 테스트 실행**

실행:

```bash
./gradlew compileJava compileTestJava
./gradlew test
```

예상:

```text
compileJava PASS
compileTestJava PASS
test PASS
```

`./gradlew test`가 `src/test/java/com/example/monghyang/devtest/JoyOrderRefundSchedulerTest.java`의 실제 Spring context 기동과 로컬 datasource credential 부재 때문에 실패하면, 이 계획에서 해당 무관 테스트를 수정하지 않는다. 정확한 실패 원인을 보고하고 focused service tests와 compile tasks를 검증 범위로 사용한다.

- [ ] **단계 5.3: 삭제 상태 진입점 점검**

실행:

```bash
rg -n "joyRepository\\.findById|joyRepository\\.findByUserId|joyRepository\\.findByBreweryIdAndJoyId" src/main/java/com/example/monghyang/domain/joy src/main/java/com/example/monghyang/domain/brewery
```

예상:

```text
JoyService, JoySlotService, JoyOrderService 안의 예약/조회 경로에는 findActiveById, findActiveByUserId, findActiveByBreweryIdAndJoyId, findDeletedByBreweryIdAndJoyId만 남아 있어야 한다.
JoyReviewService 또는 예약 히스토리처럼 이번 범위 밖인 호출은 발견되면 완료 보고의 known risk에 기록한다.
```

- [ ] **단계 5.4: 자체 검토**

점검:

```text
중복: active/deleted 조회 조건이 repository 메서드 이름과 JPQL에서 일관되는가?
회귀: restoreJoy가 active 조회로 바뀌어 복구 불가능해지지 않았는가?
경계값: 이미 삭제된 체험을 deleteJoy로 다시 삭제할 때 joyCount가 중복 감소하지 않는가?
과설계: 소프트 딜리트 공통 추상화나 새 계층을 만들지 않았는가?
단순성: JoyWeeklyStartTime을 삭제하지 않고 진입점 필터만으로 결함 2를 해결했는가?
```

문제가 발견되면 완료 보고 전에 수정하고 단계 5.1의 focused test 명령을 다시 실행한다.

## 완료 기준

- `GET /api/brewery-priv/joy`는 삭제되지 않은 체험만 반환한다.
- `DELETE /api/brewery-priv/joy/{joyId}`는 이미 삭제된 체험에 대해 `JOY_NOT_FOUND`를 반환하고 `joyCount`를 다시 감소시키지 않는다.
- `POST /api/brewery-priv/joy-restore/{joyId}`는 deleted 체험만 복구 대상으로 조회한다.
- `GET /api/joy-order/calendar`와 `GET /api/joy-order/calendar/time-info`는 deleted 체험 ID에 대해 `JOY_NOT_FOUND`를 반환한다.
- `POST /api/joy-order/prepare` 경로의 슬롯 확보 및 주문 사전등록은 deleted 체험 ID를 사용할 수 없다.
- 사용자/양조장 예약 변경은 deleted 체험의 기존 예약을 새 시간대로 변경할 수 없다.
- focused service tests가 통과하고, 전체 테스트를 실행했거나 실행 불가 사유가 정확히 보고된다.

## 자체 검토 결과

- 결함 2의 보고서 항목 중 `findByUserId`, `findByBreweryIdAndJoyId`, `prepareOrder` 직접 충돌을 모두 task에 매핑했다.
- 보고서에는 명시되지 않았지만 같은 사용자 조회 표면인 `JoySlotService` 캘린더/시간대 조회도 deleted 체험을 노출할 수 있어 같은 결함 범위로 포함했다.
- `JoyWeeklyStartTime` 삭제는 복구 시나리오를 깨뜨릴 수 있어 제외했다.
- 양조장 삭제 상태 검증은 `[결함 3]`와 중복되므로 제외했다.
- 새 dependency, migration, 공통 abstraction은 추가하지 않는다.
