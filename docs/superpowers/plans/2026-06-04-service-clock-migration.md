# Service Clock 전환 구현 계획

> **agentic worker 필수 사항:** REQUIRED SUB-SKILL: `superpowers:subagent-driven-development`를 권장합니다. 이 계획은 시간 기준 로직을 여러 서비스에 걸쳐 다루므로 작업별 구현과 리뷰를 분리해야 합니다. 단일 세션에서 실행할 경우 `superpowers:executing-plans`를 사용해 체크박스(`- [ ]`) 단위로 추적합니다.

**Goal:** `JoyOrderService` 외 다른 `Service` 계층의 직접 시간 호출을 `Clock` 주입 기반으로 바꿔 단위 테스트가 실행 시점과 무관하게 반복 가능하도록 만듭니다.

**Architecture:** 이미 `TimeZoneConfig`에 등록된 `Asia/Seoul` 기준 `Clock` bean을 재사용합니다. 각 서비스는 `private final Clock clock`을 생성자 주입으로 받고, `LocalDate.now()`와 `LocalDateTime.now()`의 무인자 호출만 `LocalDate.now(clock)` 또는 `LocalDateTime.now(clock)`로 치환합니다. 테스트는 `Clock.fixed(...)`를 명시적으로 주입해 삭제 시각, 적용일, 리뷰 작성 가능 시각을 고정 검증합니다.

**Tech Stack:** Java 21, Spring Boot 3.5.3, Spring Framework 6.2.8, JUnit 5.12.2, Mockito 5.17.0, `docs/junit-unit-test-guide.md`

---

## 전제와 범위

- 기준 문서: `docs/context7-dependencies.yaml`
- 테스트 작성 기준: `docs/junit-unit-test-guide.md`
- Context7 사용 여부: 정확한 Spring API signature 확인이 아니라 `java.time.Clock` 표준 API와 기존 Spring bean 재사용 계획이므로 사용하지 않습니다.
- 이미 전환된 파일: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- 이번 구현 대상 파일:
  - `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`
  - `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
  - `src/main/java/com/example/monghyang/domain/joy/review/service/JoyReviewService.java`
  - `src/main/java/com/example/monghyang/domain/auth/service/AuthService.java`
- 이번 테스트 대상 파일:
  - `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`
  - `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
  - `src/test/java/com/example/monghyang/domain/joy/review/service/JoyReviewServiceTest.java`
  - `src/test/java/com/example/monghyang/devtest/AuthServiceTest.java`
- 제외 범위:
  - Entity, DTO, `ApplicationErrorDto`, batch row 생성자, review 외 controller 계층의 시간 호출
  - `JoyOrderService` 재수정
  - 새 시간 추상화 클래스 생성
  - 신규 dependency 추가
- 커밋 정책: 코드 변경 커밋은 사용자 확인이 있을 때만 수행합니다.

## 파일 책임 구조

- `TimeZoneConfig`: 애플리케이션 기준 `Clock` bean 제공 책임을 유지합니다. 구현 중 bean이 없으면 `Clock.system(ZoneId.of("Asia/Seoul"))` bean만 추가하고, 이미 있으면 수정하지 않습니다.
- `BreweryService`: 양조장 탈퇴 삭제 시각, 별도 휴무일 과거일 검증, 운영시간 스냅샷 적용일 검증의 현재 날짜 기준을 `Clock`으로 받습니다.
- `JoyService`: 체험 생성 초기 스냅샷 적용일, 체험 일정 적용일 검증, 체험 삭제 시각을 `Clock`으로 받습니다.
- `JoyReviewService`: 리뷰 작성 가능 여부 판단 시 현재 시각을 `Clock`으로 받습니다.
- `AuthService`: 양조장 가입 시 초기 운영시간/휴게시간 스냅샷 적용일을 `Clock`으로 받습니다.
- 테스트 파일: `@InjectMocks`에 의존하지 않고 고정 `Clock`을 포함한 명시적 생성자로 테스트 대상 서비스를 만듭니다.

## Task 1: 작업 격리와 RED 기준 확인

**Files:**
- Read: `docs/context7-dependencies.yaml`
- Read: `docs/junit-unit-test-guide.md`
- Read: `src/main/java/com/example/monghyang/domain/config/TimeZoneConfig.java`

- [ ] **Step 1: worktree 격리 여부 확인**

Run:

```bash
git rev-parse --git-dir
git rev-parse --git-common-dir
git status --short
git branch --show-current
```

Expected:

```text
작업 트리가 깨끗하거나, 현재 작업과 무관한 변경만 식별된다.
구현은 main/master가 아닌 작업 브랜치 또는 승인된 worktree에서 진행한다.
```

- [ ] **Step 2: 현재 무인자 시간 호출 범위 확인**

Run:

```bash
rg -n "LocalDate(Time)?\\.now\\(\\)" src/main/java/com/example/monghyang/domain --glob '*Service.java'
```

Expected:

```text
src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java
src/main/java/com/example/monghyang/domain/joy/service/JoyService.java
src/main/java/com/example/monghyang/domain/joy/review/service/JoyReviewService.java
src/main/java/com/example/monghyang/domain/auth/service/AuthService.java
```

- [ ] **Step 3: 기준 테스트 실행**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

현재 기준 테스트가 실패하면 이번 변경의 원인과 구분할 수 없으므로 실패 내용을 먼저 보고하고 진행을 멈춥니다.

## Task 2: `BreweryService`를 `Clock` 기준으로 전환

**Files:**
- Modify: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`

- [ ] **Step 1: RED 테스트를 먼저 작성**

`src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`에서 `@InjectMocks` import와 필드를 제거하고, 고정 `Clock`과 명시적 생성자를 추가합니다.

```java
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
```

```java
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2026, 6, 1, 9, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE_TIME.atZone(SEOUL_ZONE_ID).toInstant(),
            SEOUL_ZONE_ID
    );

    BreweryService breweryService;

    @BeforeEach
    void setUp() {
        breweryService = new BreweryService(
                breweryRepository,
                usersRepository,
                bCryptPasswordEncoder,
                breweryImageRepository,
                storageService,
                breweryTagRepository,
                joyRepository,
                productService,
                regionTypeRepository,
                breweryClosedDateRepository,
                joyOrderRepository,
                joyStatusHistoryRepository,
                joyOrderService,
                joyOrderBatchService,
                breweryWeeklyOpenTimeRepository,
                breweryWeeklyBreakTimeRepository,
                FIXED_CLOCK
        );
    }
```

`brewery_quit_requests_refund_for_future_paid_orders()`의 환불 요청 검증을 정확한 삭제 시각 검증으로 강화합니다.

```java
        verify(joyOrderService).setRefundRequestedByBreweryDeletion(breweryId, FIXED_DATE_TIME);
```

`updateScheduleDto()`의 적용일을 고정 날짜 기준으로 바꿉니다.

```java
        dto.setEffective_date(FIXED_DATE.plusDays(1));
```

- [ ] **Step 2: RED 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.brewery.service.BreweryServiceTest
```

Expected:

```text
FAIL
constructor BreweryService(...) cannot be applied to given types
```

실패 이유가 생성자에 `Clock`이 없기 때문이어야 합니다. 다른 실패가 나오면 원인을 분리하고 생산 코드를 수정하지 않습니다.

- [ ] **Step 3: 생산 코드 최소 구현**

`src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`에 `Clock` import와 final 필드를 추가합니다.

```java
import java.time.Clock;
```

```java
    private final Clock clock;
```

무인자 시간 호출을 모두 치환합니다.

```java
        LocalDateTime deletedAt = LocalDateTime.now(clock);
```

```java
        if(dto.getClosed_date().isBefore(LocalDate.now(clock))) {
```

```java
        if (dto.getEffective_date().isBefore(LocalDate.now(clock))) {
```

- [ ] **Step 4: GREEN 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.brewery.service.BreweryServiceTest
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 3: `JoyService`를 `Clock` 기준으로 전환

**Files:**
- Modify: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`

- [ ] **Step 1: RED 테스트를 먼저 작성**

`src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`에서 `@InjectMocks` import와 필드를 제거하고, 고정 `Clock`과 명시적 생성자를 추가합니다.

```java
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.ZoneId;
```

```java
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2026, 6, 1, 9, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE_TIME.atZone(SEOUL_ZONE_ID).toInstant(),
            SEOUL_ZONE_ID
    );

    JoyService joyService;

    @BeforeEach
    void setUp() {
        joyService = new JoyService(
                joyRepository,
                breweryRepository,
                storageService,
                joyWeeklyStartTimeRepository,
                joyOrderService,
                breweryWeeklyBreakTimeRepository,
                breweryWeeklyOpenTimeRepository,
                FIXED_CLOCK
        );
    }
```

기존 테스트의 `LocalDate.now()`와 `LocalDate.now().plusDays(1)`를 각각 `FIXED_DATE`와 `FIXED_DATE.plusDays(1)`로 바꿉니다. `delete_joy_requests_refund_for_future_paid_orders()`는 삭제 시각을 정확히 검증합니다.

```java
        verify(joyOrderService).setRefundRequestedByJoyDeletion(joyId, FIXED_DATE_TIME);
```

`create_joy_saves_initial_weekly_start_time_snapshot()`에서는 저장된 스냅샷 적용일을 고정 날짜로 검증합니다.

```java
        assertEquals(FIXED_DATE, saved.getFirst().getEffectiveDate());
```

`breakTime(...)` helper의 `effectiveDate(LocalDate.now())`도 고정 날짜로 바꿉니다.

```java
                .effectiveDate(FIXED_DATE)
```

- [ ] **Step 2: RED 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.service.JoyServiceTest
```

Expected:

```text
FAIL
constructor JoyService(...) cannot be applied to given types
```

- [ ] **Step 3: 생산 코드 최소 구현**

`src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`에 `Clock` import와 final 필드를 추가합니다.

```java
import java.time.Clock;
```

```java
    private final Clock clock;
```

현재 날짜/시각 호출을 치환합니다.

```java
        LocalDate effectiveDate = LocalDate.now(clock);
```

```java
        if(dto.getEffective_date().isBefore(LocalDate.now(clock))) {
```

```java
        LocalDateTime deletedAt = LocalDateTime.now(clock);
```

- [ ] **Step 4: GREEN 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.service.JoyServiceTest
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 4: `JoyReviewService`에 시간 기준 단위 테스트를 추가하고 `Clock`으로 전환

**Files:**
- Create: `src/test/java/com/example/monghyang/domain/joy/review/service/JoyReviewServiceTest.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/review/service/JoyReviewService.java`

- [ ] **Step 1: RED 테스트 파일 생성**

`src/test/java/com/example/monghyang/domain/joy/review/service/JoyReviewServiceTest.java`를 생성합니다.

```java
package com.example.monghyang.domain.joy.review.service;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.global.advice.ApplicationException;
import com.example.monghyang.domain.joy.entity.Joy;
import com.example.monghyang.domain.joy.entity.JoyOrder;
import com.example.monghyang.domain.joy.entity.JoyPaymentStatus;
import com.example.monghyang.domain.joy.repository.JoyOrderRepository;
import com.example.monghyang.domain.joy.repository.JoyRepository;
import com.example.monghyang.domain.joy.review.dto.ReqJoyReviewDto;
import com.example.monghyang.domain.joy.review.repository.JoyReviewLikeHistoryRepository;
import com.example.monghyang.domain.joy.review.repository.JoyReviewRepository;
import com.example.monghyang.domain.users.entity.Users;
import com.example.monghyang.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JoyReviewServiceTest {
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2026, 6, 1, 9, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE_TIME.atZone(SEOUL_ZONE_ID).toInstant(),
            SEOUL_ZONE_ID
    );

    @Mock
    JoyReviewRepository joyReviewRepository;
    @Mock
    JoyRepository joyRepository;
    @Mock
    UsersRepository usersRepository;
    @Mock
    JoyReviewLikeHistoryRepository joyReviewLikeHistoryRepository;
    @Mock
    JoyOrderRepository joyOrderRepository;

    JoyReviewService joyReviewService;

    @BeforeEach
    void setUp() {
        joyReviewService = new JoyReviewService(
                joyReviewRepository,
                joyRepository,
                usersRepository,
                joyReviewLikeHistoryRepository,
                joyOrderRepository,
                FIXED_CLOCK
        );
    }

    @Test
    @DisplayName("체험 시작 전이면 결제 완료 예약이어도 리뷰를 작성할 수 없다")
    void add_review_rejects_paid_order_before_reservation_time() {
        Long userId = 1L;
        ReqJoyReviewDto dto = reviewDto();
        JoyOrder joyOrder = mock(JoyOrder.class);
        given(usersRepository.findById(userId)).willReturn(Optional.of(mock(Users.class)));
        given(joyRepository.findById(dto.getJoy_id())).willReturn(Optional.of(mock(Joy.class)));
        given(joyOrderRepository.findByIdAndUserId(dto.getJoy_order_id(), userId)).willReturn(Optional.of(joyOrder));
        given(joyOrder.getJoyPaymentStatus()).willReturn(JoyPaymentStatus.PAID);
        given(joyOrder.getReservation()).willReturn(FIXED_DATE_TIME.plusMinutes(1));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> joyReviewService.addReview(userId, dto)
        );

        assertEquals(ApplicationError.JOY_REVIEW_CREATE_UNQUALIFIED, exception.getApplicationError());
        verify(joyReviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("체험 시작 이후 결제 완료 예약이면 리뷰를 저장한다")
    void add_review_saves_review_after_paid_reservation_time() {
        Long userId = 1L;
        ReqJoyReviewDto dto = reviewDto();
        JoyOrder joyOrder = mock(JoyOrder.class);
        given(usersRepository.findById(userId)).willReturn(Optional.of(mock(Users.class)));
        given(joyRepository.findById(dto.getJoy_id())).willReturn(Optional.of(mock(Joy.class)));
        given(joyOrderRepository.findByIdAndUserId(dto.getJoy_order_id(), userId)).willReturn(Optional.of(joyOrder));
        given(joyOrder.getJoyPaymentStatus()).willReturn(JoyPaymentStatus.PAID);
        given(joyOrder.getReservation()).willReturn(FIXED_DATE_TIME.minusMinutes(1));

        joyReviewService.addReview(userId, dto);

        verify(joyReviewRepository).save(any());
    }

    private ReqJoyReviewDto reviewDto() {
        ReqJoyReviewDto dto = new ReqJoyReviewDto();
        dto.setJoy_id(10L);
        dto.setJoy_order_id(99L);
        dto.setContent("체험이 좋았습니다.");
        dto.setStar(4.5);
        return dto;
    }
}
```

- [ ] **Step 2: RED 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.review.service.JoyReviewServiceTest
```

Expected:

```text
FAIL
constructor JoyReviewService(...) cannot be applied to given types
```

- [ ] **Step 3: 생산 코드 최소 구현**

`src/main/java/com/example/monghyang/domain/joy/review/service/JoyReviewService.java`에 `Clock` import와 final 필드를 추가합니다.

```java
import java.time.Clock;
```

```java
    private final Clock clock;
```

리뷰 작성 가능 시각 검증을 주입된 `Clock` 기준으로 바꿉니다.

```java
        if(!joyOrder.getJoyPaymentStatus().equals(JoyPaymentStatus.PAID)
                || LocalDateTime.now(clock).isBefore(joyOrder.getReservation())) {
```

- [ ] **Step 4: GREEN 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.joy.review.service.JoyReviewServiceTest
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 5: `AuthService`의 양조장 가입 스냅샷 적용일을 `Clock` 기준으로 전환

**Files:**
- Modify: `src/test/java/com/example/monghyang/devtest/AuthServiceTest.java`
- Modify: `src/main/java/com/example/monghyang/domain/auth/service/AuthService.java`

- [ ] **Step 1: RED 테스트를 먼저 작성**

`src/test/java/com/example/monghyang/devtest/AuthServiceTest.java`에서 `@InjectMocks` import와 필드를 제거하고, 고정 `Clock`과 명시적 생성자를 추가합니다.

```java
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
```

```java
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2026, 6, 1, 9, 30);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE_TIME.atZone(SEOUL_ZONE_ID).toInstant(),
            SEOUL_ZONE_ID
    );

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                usersRepository,
                bCryptPasswordEncoder,
                roleRepository,
                jwtUtil,
                redisService,
                sessionUtil,
                sellerRepository,
                breweryRepository,
                regionTypeRepository,
                storageService,
                breweryImageRepository,
                sellerImageRepository,
                breweryWeeklyOpenTimeRepository,
                breweryWeeklyBreakTimeRepository,
                FIXED_CLOCK
        );
    }
```

`breweryJoin_success()`에 운영시간/휴게시간 스냅샷 저장 검증을 추가합니다.

```java
        ArgumentCaptor<BreweryWeeklyOpenTime> openTimeCaptor = ArgumentCaptor.forClass(BreweryWeeklyOpenTime.class);
        ArgumentCaptor<BreweryWeeklyBreakTime> breakTimeCaptor = ArgumentCaptor.forClass(BreweryWeeklyBreakTime.class);
```

```java
        verify(breweryWeeklyOpenTimeRepository).save(openTimeCaptor.capture());
        verify(breweryWeeklyBreakTimeRepository).save(breakTimeCaptor.capture());
```

```java
        assertEquals(FIXED_DATE, openTimeCaptor.getValue().getEffectiveDate());
        assertEquals(FIXED_DATE, breakTimeCaptor.getValue().getEffectiveDate());
```

- [ ] **Step 2: RED 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.devtest.AuthServiceTest
```

Expected:

```text
FAIL
constructor AuthService(...) cannot be applied to given types
```

- [ ] **Step 3: 생산 코드 최소 구현**

`src/main/java/com/example/monghyang/domain/auth/service/AuthService.java`에 `Clock` import와 final 필드를 추가합니다.

```java
import java.time.Clock;
```

```java
    private final Clock clock;
```

`breweryJoin(...)`에서 루프 진입 전 적용일을 한 번만 계산합니다.

```java
        LocalDate effectiveDate = LocalDate.now(clock);
        for(BreweryScheduleDto schedule : breweryJoinDto.getSchedules()) {
```

운영시간과 휴게시간 저장에 같은 `effectiveDate` 변수를 사용합니다.

```java
                    .effectiveDate(effectiveDate)
```

- [ ] **Step 4: GREEN 확인**

Run:

```bash
./gradlew test --tests com.example.monghyang.devtest.AuthServiceTest
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 6: 직접 시간 호출 잔여분과 회귀 테스트 검증

**Files:**
- Verify: `src/main/java/com/example/monghyang/domain/**/service/*.java`
- Verify: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`
- Verify: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
- Verify: `src/test/java/com/example/monghyang/domain/joy/review/service/JoyReviewServiceTest.java`
- Verify: `src/test/java/com/example/monghyang/devtest/AuthServiceTest.java`

- [ ] **Step 1: 생산 Service의 무인자 시간 호출 제거 확인**

Run:

```bash
rg -n "LocalDate(Time)?\\.now\\(\\)" src/main/java/com/example/monghyang/domain --glob '*Service.java'
```

Expected:

```text
No matches
```

- [ ] **Step 2: 영향 테스트 묶음 실행**

Run:

```bash
./gradlew test \
  --tests com.example.monghyang.domain.brewery.service.BreweryServiceTest \
  --tests com.example.monghyang.domain.joy.service.JoyServiceTest \
  --tests com.example.monghyang.domain.joy.review.service.JoyReviewServiceTest \
  --tests com.example.monghyang.devtest.AuthServiceTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: 전체 테스트 실행**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 4: 빌드 실행**

Run:

```bash
./gradlew build
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5: diff 정합성 확인**

Run:

```bash
git diff --check
git diff --stat
```

Expected:

```text
git diff --check 출력 없음
변경 파일은 계획 대상 서비스와 테스트 파일로 제한됨
```

- [ ] **Step 6: 커밋 승인 확인 후 커밋**

사용자가 코드 변경 커밋을 명시적으로 승인한 경우에만 실행합니다.

```bash
git add \
  src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java \
  src/main/java/com/example/monghyang/domain/joy/service/JoyService.java \
  src/main/java/com/example/monghyang/domain/joy/review/service/JoyReviewService.java \
  src/main/java/com/example/monghyang/domain/auth/service/AuthService.java \
  src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java \
  src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java \
  src/test/java/com/example/monghyang/domain/joy/review/service/JoyReviewServiceTest.java \
  src/test/java/com/example/monghyang/devtest/AuthServiceTest.java
git commit -m "Refactor(service): 시간 기준 Clock 주입 확대"
```

Expected:

```text
[branch commit] Refactor(service): 시간 기준 Clock 주입 확대
```

## 자기 검토 체크리스트

- [ ] `Clock`은 실제 런타임 시간 기준이라는 생산 책임이므로 테스트 편의를 위한 비정상 설계가 아닙니다.
- [ ] 새 interface, helper, factory, utility class를 만들지 않습니다.
- [ ] `LocalDate.now(clock)`과 `LocalDateTime.now(clock)` 외의 시간 의미는 바꾸지 않습니다.
- [ ] `AuthService.breweryJoin(...)`에서는 같은 가입 요청의 운영시간/휴게시간에 동일한 `effectiveDate`를 사용합니다.
- [ ] `JoyReviewService` 신규 테스트는 리뷰 작성 가능/불가능 경계만 검증합니다.
- [ ] 모든 `@DisplayName`과 새 주석은 한국어로 작성합니다.
- [ ] 계획 범위를 벗어난 Entity/DTO/Controller 시간 호출은 수정하지 않습니다.
