# 체험 확정 휴무 예약 검증 일관화 구현 계획

> **Agentic worker 필수 지침:** 이 계획을 구현할 때는 `superpowers:executing-plans`를 사용한다. 구현 전에는 `superpowers:using-git-worktrees`를 적용하고, 각 단계는 체크박스(`- [ ]`)로 추적한다.

**목표:** 캘린더에서 예약 불가로 계산하는 `CONFIRMED` 별도 휴무일과 휴무 시간대를 체험 예약 생성/변경 검증에서도 동일하게 거부한다.

**아키텍처:** 별도 휴무 판정의 기준은 현재 캘린더와 동일하게 `ClosedStatus.CONFIRMED`로 고정한다. `JoySlotService`에 일 단위 확정 휴무 검증 메서드를 추가하고, `JoyOrderService.verifyReservation(...)`에서 기존 운영시간, 휴게시간, 체험 시작시간 검증과 함께 호출한다. 새 클래스, 새 테이블, 새 의존성은 만들지 않는다.

**기술 스택:** Java 21, Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5.12.2, Mockito 5.17.0/5.14.2.

**의존성 기준:** `docs/context7-dependencies.yaml`

**Context7:** `context7_library_id: not_used` - 정확한 외부 API signature나 버전별 설정값 확인이 아니라 기존 repository와 서비스 로직의 도메인 판정 기준을 맞추는 작업이므로 호출하지 않는다.

---

## 검토 결과

- `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java:186`부터 `BreweryClosedDate`, `JoyClosedDate`, `JoyClosedStartTime`을 모두 `ClosedStatus.CONFIRMED`로 조회한다.
- `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java:221`과 `:227`은 확정 양조장 휴무일과 확정 체험 전일 휴무일을 예약 불가 날짜로 처리한다.
- `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java:268`부터 확정 체험 휴무 시간대(`JoyClosedStartTime`)를 유효 슬롯에서 제거한다.
- `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java:73`의 `verifyReservation(...)`은 인원, 과거 일시, 운영시간 범위, 휴게시간, 활성 체험 시작시간만 검증하고 별도 휴무일/휴무 시간대는 검증하지 않는다.
- `/api/joy-order/prepare`는 `JoyOrderController`에서 `reservationJoySlotCount(...)`를 먼저 호출하므로 `JoyOrderService.verifyReservation(...)`의 누락이 직접 예약 생성 허용으로 이어진다.
- `/api/joy-order/change`와 `/api/brewery-priv/joy-order/change`는 각각 `updateReservation(...)`, `updateReservationByBrewery(...)`에서 같은 `verifyReservation(...)`을 호출하므로 같은 누락을 공유한다.
- `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java:156` 주석은 `PENDING`이 신규 예약을 차단한다고 설명하지만, 실제 캘린더 조회는 `CONFIRMED`만 사용한다. 이 계획은 현재 코드의 실질 정책인 `CONFIRMED` 차단을 기준으로 주석을 고친다.

## 범위와 전제

- 예약 차단 대상은 `ClosedStatus.CONFIRMED` 상태의 `BreweryClosedDate`, `JoyClosedDate.isAllDay == true`, `JoyClosedStartTime`이다.
- `PENDING`은 휴무 후보/검토 상태이며 이번 계획에서는 신규 예약을 차단하지 않는다. `PENDING`부터 차단해야 하는 정책이면 캘린더와 예약 검증을 모두 `PENDING + CONFIRMED` 기준으로 바꾸는 별도 승인이 필요하다.
- 예약 API에서 확정 휴무로 거부될 때는 기존 시간 검증 오류인 `ApplicationError.JOY_ORDER_TIME_INVALID`를 사용한다.
- `count < minCount`에서 `JOY_COUNT_OVER`가 반환되는 기존 문제는 별도 결함이며 이번 계획 범위에 포함하지 않는다.
- 코드 변경 커밋은 사용자 확인 전에는 만들지 않는다. 이는 `AGENTS.md` §11.6이 `superpowers:writing-plans`의 일반적인 잦은 커밋 지침보다 우선하기 때문이다.

## 파일 구조

- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`
  - 일 단위 확정 휴무 검증 메서드를 추가한다.
  - `BreweryClosedDate`, `JoyClosedDate`, `JoyClosedStartTime` repository의 기존 월 범위 조회 메서드를 `reservationDate`부터 `reservationDate.plusDays(1)` 미만 범위로 재사용한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
  - `verifyReservation(...)`에서 확정 휴무 검증을 호출한다.
  - 검증 단계 주석/Javadoc을 실제 검증 순서에 맞게 갱신한다.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`
  - `/brewery-close-try`의 `PENDING` 설명 주석을 실제 정책과 맞춘다.
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`
  - 확정 양조장 휴무일, 확정 체험 전일 휴무일, 확정 체험 휴무 시간대가 예약 검증에서 거부되는지 단위 테스트를 추가한다.
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
  - 예약 생성 슬롯 확보, 사용자 예약 변경, 양조장 예약 변경이 `JoySlotService`의 확정 휴무 검증 실패를 전파하고 슬롯 변경을 수행하지 않는지 테스트한다.

---

### Task 1: 확정 휴무 검증 실패 테스트 추가

**파일:**
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoySlotServiceTest.java`

- [x] **Step 1: 필요한 entity import를 추가한다**

```java
import com.example.monghyang.domain.brewery.entity.BreweryClosedDate;
import com.example.monghyang.domain.joy.entity.JoyClosedDate;
import com.example.monghyang.domain.joy.entity.JoyClosedStartTime;
```

- [x] **Step 2: 확정 양조장 휴무일 거부 테스트를 추가한다**

```java
@Test
@DisplayName("확정 양조장 휴무일은 예약 검증에서 거부한다")
void verify_reservable_by_confirmed_closed_schedule_rejects_brewery_closed_date() {
    Long breweryId = 20L;
    Long joyId = 10L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);

    given(breweryClosedDateRepository.findConfirmedByBreweryIdAndMonth(
            breweryId,
            reservationDate,
            reservationDate.plusDays(1),
            ClosedStatus.CONFIRMED
    )).willReturn(List.of(mock(BreweryClosedDate.class)));

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joySlotService.verifyReservableByConfirmedClosedSchedule(
                    breweryId,
                    joyId,
                    reservationDate,
                    reservationTime
            )
    );

    assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
}
```

- [x] **Step 3: 확정 체험 전일 휴무일 거부 테스트를 추가한다**

```java
@Test
@DisplayName("확정 체험 전일 휴무일은 예약 검증에서 거부한다")
void verify_reservable_by_confirmed_closed_schedule_rejects_joy_all_day_closed_date() {
    Long breweryId = 20L;
    Long joyId = 10L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);
    JoyClosedDate joyClosedDate = mock(JoyClosedDate.class);

    given(joyClosedDate.getIsAllDay()).willReturn(true);
    given(breweryClosedDateRepository.findConfirmedByBreweryIdAndMonth(
            breweryId,
            reservationDate,
            reservationDate.plusDays(1),
            ClosedStatus.CONFIRMED
    )).willReturn(List.of());
    given(joyClosedDateRepository.findConfirmedByJoyIdAndMonth(
            joyId,
            reservationDate,
            reservationDate.plusDays(1),
            ClosedStatus.CONFIRMED
    )).willReturn(List.of(joyClosedDate));

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joySlotService.verifyReservableByConfirmedClosedSchedule(
                    breweryId,
                    joyId,
                    reservationDate,
                    reservationTime
            )
    );

    assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
}
```

- [x] **Step 4: 확정 체험 휴무 시간대 거부 테스트를 추가한다**

```java
@Test
@DisplayName("확정 체험 휴무 시간대는 예약 검증에서 거부한다")
void verify_reservable_by_confirmed_closed_schedule_rejects_joy_closed_start_time() {
    Long breweryId = 20L;
    Long joyId = 10L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);
    JoyClosedStartTime closedStartTime = mock(JoyClosedStartTime.class);

    given(closedStartTime.getClosedStartTime()).willReturn(reservationTime);
    given(breweryClosedDateRepository.findConfirmedByBreweryIdAndMonth(
            breweryId,
            reservationDate,
            reservationDate.plusDays(1),
            ClosedStatus.CONFIRMED
    )).willReturn(List.of());
    given(joyClosedDateRepository.findConfirmedByJoyIdAndMonth(
            joyId,
            reservationDate,
            reservationDate.plusDays(1),
            ClosedStatus.CONFIRMED
    )).willReturn(List.of());
    given(joyClosedStartTimeRepository.findConfirmedByJoyIdAndMonth(
            joyId,
            reservationDate,
            reservationDate.plusDays(1),
            ClosedStatus.CONFIRMED
    )).willReturn(List.of(closedStartTime));

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joySlotService.verifyReservableByConfirmedClosedSchedule(
                    breweryId,
                    joyId,
                    reservationDate,
                    reservationTime
            )
    );

    assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
}
```

- [x] **Step 5: 테스트가 아직 실패하는지 확인한다**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.service.JoySlotServiceTest
```

Expected: `verifyReservableByConfirmedClosedSchedule(...)`가 아직 없어서 컴파일 실패한다.

### Task 2: 일 단위 확정 휴무 검증 구현

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoySlotService.java`

- [x] **Step 1: `JoySlotService`에 public 검증 메서드를 추가한다**

`getImpossibleDate(...)` 앞에 아래 메서드를 추가한다.

```java
/**
 * 예약 대상 일시가 확정된 별도 휴무일이나 휴무 시간대에 포함되는지 검증합니다.
 * 양조장 휴무일, 체험 전일 휴무일, 체험 시간대별 휴무는 모두 CONFIRMED 상태만 예약 차단 기준으로 사용합니다.
 *
 * @param breweryId       양조장 식별자
 * @param joyId           체험 식별자
 * @param reservationDate 예약 대상일
 * @param reservationTime 예약 시작 시간
 */
public void verifyReservableByConfirmedClosedSchedule(
        Long breweryId,
        Long joyId,
        LocalDate reservationDate,
        LocalTime reservationTime
) {
    LocalDate endDate = reservationDate.plusDays(1);

    // 1. 확정된 양조장 전일 휴무일이면 해당 양조장의 모든 체험 예약을 차단한다.
    boolean breweryClosed = !breweryClosedDateRepository.findConfirmedByBreweryIdAndMonth(
            breweryId,
            reservationDate,
            endDate,
            ClosedStatus.CONFIRMED
    ).isEmpty();
    if (breweryClosed) {
        throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
    }

    // 2. 확정된 체험 전일 휴무일이면 해당 체험의 모든 시간대 예약을 차단한다.
    boolean joyAllDayClosed = joyClosedDateRepository.findConfirmedByJoyIdAndMonth(
                    joyId,
                    reservationDate,
                    endDate,
                    ClosedStatus.CONFIRMED
            )
            .stream()
            .anyMatch(JoyClosedDate::getIsAllDay);
    if (joyAllDayClosed) {
        throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
    }

    // 3. 확정된 체험 시간대별 휴무이면 해당 시작 시간 예약만 차단한다.
    boolean joyStartTimeClosed = joyClosedStartTimeRepository.findConfirmedByJoyIdAndMonth(
                    joyId,
                    reservationDate,
                    endDate,
                    ClosedStatus.CONFIRMED
            )
            .stream()
            .anyMatch(jcst -> jcst.getClosedStartTime().equals(reservationTime));
    if (joyStartTimeClosed) {
        throw new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID);
    }
}
```

- [x] **Step 2: `JoySlotServiceTest`를 다시 실행해 통과를 확인한다**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.service.JoySlotServiceTest
```

Expected: PASS

### Task 3: 예약 생성/변경 경로에 확정 휴무 검증 연결

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`

- [x] **Step 1: `JoyOrderServiceTest`에 `willThrow` static import를 추가한다**

```java
import static org.mockito.BDDMockito.willThrow;
```

- [x] **Step 2: 예약 슬롯 확보가 확정 휴무 검증 실패를 전파하는 테스트를 추가한다**

```java
@Test
@DisplayName("예약 슬롯 증가는 확정 별도 휴무이면 새 슬롯을 증가시키지 않는다")
void reservation_joy_slot_count_rejects_confirmed_closed_schedule() {
    Long joyId = 10L;
    Long breweryId = 20L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);

    given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(mock(Joy.class)));
    given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
            .willReturn(Optional.of(new JoyInfoDto(breweryId, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
    given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(breweryId, reservationDate, DayOfWeek.Mon))
            .willReturn(List.of());
    willThrow(new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID))
            .given(joySlotService)
            .verifyReservableByConfirmedClosedSchedule(breweryId, joyId, reservationDate, reservationTime);

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.reservationJoySlotCount(joyId, reservationDate, reservationTime, 2)
    );

    assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
    verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
}
```

- [x] **Step 3: 사용자 예약 변경이 확정 휴무 검증 실패를 전파하는 테스트를 추가한다**

```java
@Test
@DisplayName("사용자 예약 변경은 확정 별도 휴무이면 새 슬롯을 증가시키지 않는다")
void update_reservation_rejects_confirmed_closed_schedule() {
    Long userId = 1L;
    Long joyId = 10L;
    Long breweryId = 20L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);
    ReqUpdateJoyOrderDto dto = updateDto(99L, reservationDate, reservationTime, 2);
    JoyOrder joyOrder = joyOrder(joyId);
    Users users = mock(Users.class);
    given(users.getId()).willReturn(userId);
    given(joyOrder.getUsers()).willReturn(users);
    given(joyOrder.getReservation()).willReturn(LocalDate.of(2026, 6, 2).atTime(LocalTime.of(10, 0)));
    given(joyOrderRepository.findById(dto.getId())).willReturn(Optional.of(joyOrder));
    given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(mock(Joy.class)));
    given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
            .willReturn(Optional.of(new JoyInfoDto(breweryId, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
    given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(breweryId, reservationDate, DayOfWeek.Mon))
            .willReturn(List.of());
    willThrow(new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID))
            .given(joySlotService)
            .verifyReservableByConfirmedClosedSchedule(breweryId, joyId, reservationDate, reservationTime);

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.updateReservation(userId, dto)
    );

    assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
    verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
    verify(joySlotService, never()).decrementJoySlotCount(any(), any(), any(), any());
}
```

- [x] **Step 4: 양조장 예약 변경이 확정 휴무 검증 실패를 전파하는 테스트를 추가한다**

```java
@Test
@DisplayName("양조장 예약 변경은 확정 별도 휴무이면 새 슬롯을 증가시키지 않는다")
void update_reservation_by_brewery_rejects_confirmed_closed_schedule() {
    Long userId = 1L;
    Long joyId = 10L;
    Long breweryId = 20L;
    LocalDate reservationDate = LocalDate.of(2026, 6, 1);
    LocalTime reservationTime = LocalTime.of(10, 0);
    ReqUpdateJoyOrderDto dto = updateDto(99L, reservationDate, reservationTime, 2);
    JoyOrder joyOrder = joyOrder(joyId);
    given(joyOrderRepository.findByIdAndBreweryUserId(dto.getId(), userId)).willReturn(Optional.of(joyOrder));
    given(joyRepository.findActiveById(joyId)).willReturn(Optional.of(mock(Joy.class)));
    given(breweryRepository.findJoyTimeInfoByJoyId(joyId, reservationDate, DayOfWeek.Mon))
            .willReturn(Optional.of(new JoyInfoDto(breweryId, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, 10, 1)));
    given(breweryWeeklyBreakTimeRepository.findActiveBreakTimesByBreweryIdAndDate(breweryId, reservationDate, DayOfWeek.Mon))
            .willReturn(List.of());
    willThrow(new ApplicationException(ApplicationError.JOY_ORDER_TIME_INVALID))
            .given(joySlotService)
            .verifyReservableByConfirmedClosedSchedule(breweryId, joyId, reservationDate, reservationTime);

    ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> joyOrderService.updateReservationByBrewery(userId, dto)
    );

    assertEquals(ApplicationError.JOY_ORDER_TIME_INVALID, exception.getApplicationError());
    verify(joySlotService, never()).reservationJoySlot(any(), any(), any(), any(), any());
    verify(joySlotService, never()).decrementJoySlotCount(any(), any(), any(), any());
}
```

- [x] **Step 5: `JoyOrderService.verifyReservation(...)`에 확정 휴무 검증 호출을 추가한다**

`verifyBreakTime(...)` 다음, 활성 체험 시작 시간 스냅샷 조회 전에 아래 호출을 추가한다.

```java
        // 확정된 별도 휴무일과 휴무 시간대는 캘린더와 동일한 기준으로 예약을 차단한다.
        joySlotService.verifyReservableByConfirmedClosedSchedule(
                joyInfoDto.breweryId(),
                joyId,
                reservationDate,
                reservationTime
        );
```

검증 과정 주석은 아래 의미가 드러나도록 갱신한다.

```java
        // 검증 과정
        // 1. 예약 일시가 현재보다 이전인지
        // 2. 예약 시간대가 영업 시작 시간보다 이전인지
        // 3. 예약의 체험 종료 시간이 영업 종료 시간대보다 이후인지
        // 4. 체험 시작 시간이 양조장의 '체험 시간 단위' 간격에 일치하는지
        // 5. 체험 진행 시간이 양조장 휴게시간과 겹치는지
        // 6. 예약 일시가 확정된 별도 휴무일/휴무 시간대에 포함되는지
        // 7. 예약일에 활성화된 체험 시작 시간 스냅샷에 요청 시간이 존재하는지 검증
```

- [x] **Step 6: 관련 서비스 테스트를 실행한다**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.service.JoyOrderServiceTest
```

Expected: PASS

### Task 4: `/brewery-close-try` 주석을 실제 정책과 맞추기

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`

- [x] **Step 1: `PENDING` 설명 주석을 수정한다**

기존 주석:

```java
        // 해당 날짜를 휴무일로 지정하고, 'PENDING' 상태로 설정
        // 이때는 신규 예약만 차단하고, 아직 환불 절차는 수행하지 않는 단계
```

수정:

```java
        // 해당 날짜를 휴무일 후보로 저장하고, 'PENDING' 상태로 둔다.
        // 신규 예약 차단과 기존 예약 환불 요청은 휴무일이 'CONFIRMED'로 확정된 뒤 수행한다.
```

- [x] **Step 2: 주석 변경 후 컴파일을 확인한다**

Run:

```bash
./gradlew compileJava
```

Expected: PASS

### Task 5: 통합 검증과 자체 검토

**파일:**
- 검증: 관련 서비스 테스트와 전체 테스트

- [x] **Step 1: 직접 관련 테스트를 실행한다**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.service.JoySlotServiceTest --tests com.example.monghyang.domain.joy.service.JoyOrderServiceTest
```

Expected: PASS

- [x] **Step 2: 전체 테스트를 실행한다**

Run:

```bash
./gradlew test
```

Expected: PASS

- [x] **Step 3: 자체 검토를 수행한다**

검토 항목:

- `CONFIRMED`만 차단한다는 정책이 `JoySlotService.getImpossibleDate(...)`와 새 예약 검증 메서드에서 일치하는가?
- `/api/joy-order/prepare`, `/api/joy-order/change`, `/api/brewery-priv/joy-order/change` 세 경로 모두 슬롯 증가 전에 같은 검증을 통과해야 하는가?
- 확정 휴무 검증 실패 시 새 슬롯 증가와 기존 슬롯 감소가 모두 수행되지 않는가?
- 새 class/interface/configuration/dependency 없이 기존 repository와 service 경계 안에서 해결했는가?
- 테스트 편의를 위해 production 설계를 왜곡한 지점이 없는가?
- `PENDING` 차단 정책을 암시하는 주석이 남아 있지 않은가?

- [x] **Step 4: 커밋 여부를 사용자에게 확인한다**

코드 변경 커밋은 `AGENTS.md` §11.6에 따라 사용자 확인 후에만 수행한다. 확인을 받으면 커밋 메시지는 아래 형식을 사용한다.

```bash
git commit -m "Fix(joy): 확정 휴무 예약 검증 추가"
```
