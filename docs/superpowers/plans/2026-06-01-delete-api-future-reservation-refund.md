# 삭제 API 미래 예약 환불 요청 전환 구현 계획

> **에이전트 작업자 필수 하위 스킬:** 이 계획을 실행할 때는 `superpowers:subagent-driven-development`를 기본으로 사용한다. 삭제 API가 예약 결제 상태, 상태 이력, 트랜잭션 경계를 함께 건드리므로 작업은 체크박스(`- [ ]`)로 추적하고, 실행 전 `superpowers:using-git-worktrees`를 먼저 적용한다.

**목표:** 양조장 삭제와 체험 삭제 시 삭제 시점 이후의 기존 `PAID` 체험 예약을 `REFUND_REQUESTED`로 전환해 스냅샷 변경 흐름과 운영 불가 예약 처리 정책을 맞춘다.

**아키텍처:** 삭제 API는 기존처럼 `isDeleted`를 유지하고, 환불 대상 선정과 상태 이력 생성은 `JoyOrderService`에 삭제 전용 메서드로 모은다. 새 스케줄러나 새 계층은 만들지 않고, 기존 `JoyOrderRepository` 일괄 상태 변경과 `JoyOrderBatchService.batchInsert` 흐름을 재사용한다. 환불 대상은 삭제 호출 시각 이후 예약된 `PAID` 및 `JoyOrder.isDeleted = false` 예약으로 제한한다.

**기술 스택:** Java 21, Spring Boot 3.5.3, Spring Data JPA 3.5.1, JUnit 5.12.2, Mockito Core 5.17.0, Mockito JUnit Jupiter 5.14.2. 정확한 버전은 `docs/context7-dependencies.yaml` 기준이다.

**상태:** 작성 완료 / 미승인

---

## 승인 및 실행 경계

- 이 문서는 계획이며, 사용자가 명시적으로 승인하기 전에는 프로덕션 코드와 테스트 코드를 수정하지 않는다.
- `superpowers:writing-plans`의 영문 헤더 예시는 `AGENTS.md`의 한국어 문서 작성 규칙과 충돌하므로, 더 높은 우선순위인 `AGENTS.md` §0.1 및 §13을 적용해 본 계획서를 한국어로 작성한다.
- 구현 승인 후에는 현재 브랜치와 작업 트리를 확인하고, `main` 또는 `master`에서 직접 구현하지 않는다.
- 코드 변경 커밋은 별도 사용자 확인 전에는 만들지 않는다.
- `JoyRepository`의 active 조회 조건은 신규 예약 차단을 위한 기존 정책으로 유지한다. 이번 계획은 이미 생성된 미래 예약의 결제 상태 정리에 집중한다.
- `REFUND_PROCESSING`, `REFUNDED`, `REFUND_FAILED`로 넘어가는 후속 배치의 reason code는 현재 `JoyOrderBatchService`가 공통 문자열을 사용하므로 이번 범위에서 바꾸지 않는다.

## 의존성 및 Context7 기준

- version: Spring Boot `3.5.3`, Spring Data JPA `3.5.1`, JUnit `5.12.2`, Mockito Core `5.17.0`, Mockito JUnit Jupiter `5.14.2`
- source: `docs/context7-dependencies.yaml`
- context7_library_id: `not_used`
- reason: 기존 저장소의 `@Transactional`, `@Query`, JUnit 5, Mockito 패턴을 그대로 확장하는 계획이며, 새로운 API signature나 버전별 설정 옵션 확인이 필요하지 않다.
- 테스트 작성 기준: `docs/junit-unit-test-guide.md`

## 파일 구조

- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyOrderRepository.java`
  - 양조장 삭제 시 삭제 시점 이후의 `PAID` 예약 식별자만 조회하는 ID 전용 쿼리를 추가한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
  - 체험 삭제와 양조장 삭제 각각의 환불 요청 전환 메서드를 추가한다.
  - 두 메서드는 삭제 호출 시각 이후, `PAID`, `isDeleted = false` 조건만 대상으로 삼는다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
  - `deleteJoy`에 트랜잭션을 명시하고, 체험 삭제 후 환불 요청 전환을 호출한다.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`
  - `breweryQuit`에서 양조장 삭제 후 환불 요청 전환을 호출한다.
- 테스트 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
  - 삭제 전용 환불 요청 전환의 대상 조건, 이력 생성, 빈 대상 no-op을 검증한다.
- 테스트 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
  - 체험 삭제 API가 환불 요청 전환을 호출하는지 검증한다.
- 테스트 수정: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`
  - 양조장 삭제 API가 환불 요청 전환을 호출하는지 검증한다.

---

### 작업 0: 실행 전 규칙 확인

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

### 작업 1: 삭제 전용 환불 전환 서비스 테스트 작성

**파일:**
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`

- [ ] **단계 1.1: 기존 import 확인**

`JoyOrderServiceTest`에는 이미 `ArgumentCaptor`, `LocalDateTime`, `List`, `any`, `never` import가 있으므로 이 작업에서 별도 import를 추가하지 않는다.

- [ ] **단계 1.2: 체험 삭제 환불 전환 테스트 추가**

`JoyOrderServiceTest`의 기존 환불 처리 테스트 근처에 다음 테스트를 추가한다.

```java
@Test
@SuppressWarnings("unchecked")
@DisplayName("체험 삭제 환불 처리는 삭제 시점 이후 PAID 예약을 환불 요청 상태로 전환하고 이력을 저장한다")
void set_refund_requested_by_joy_deletion_updates_future_paid_orders_and_inserts_histories() {
    Long joyId = 10L;
    LocalDateTime deletedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
    given(joyOrderRepository.findIdByJoyIdAndReservationFromAndPaymentStatusAndIsDeleted(
            joyId,
            deletedAt,
            JoyPaymentStatus.PAID,
            false
    )).willReturn(List.of(1L, 2L));

    joyOrderService.setRefundRequestedByJoyDeletion(joyId, deletedAt);

    verify(joyOrderRepository).findIdByJoyIdAndReservationFromAndPaymentStatusAndIsDeleted(
            joyId,
            deletedAt,
            JoyPaymentStatus.PAID,
            false
    );
    verify(joyOrderRepository).updatePaymentStatusByJoyIdListAndStatus(
            List.of(1L, 2L),
            JoyPaymentStatus.REFUND_REQUESTED
    );
    ArgumentCaptor<List<JoyStatusHistoryBatchRow>> captor = ArgumentCaptor.forClass(List.class);
    verify(joyOrderBatchService).batchInsert(captor.capture());
    assertEquals(2, captor.getValue().size());
    assertEquals("체험 삭제", captor.getValue().getFirst().getReasonCode());
}
```

- [ ] **단계 1.3: 체험 삭제 환불 대상 없음 테스트 추가**

동일 테스트 클래스에 다음 테스트를 추가한다.

```java
@Test
@DisplayName("체험 삭제 환불 처리는 대상 예약이 없으면 상태 변경과 이력 저장을 하지 않는다")
void set_refund_requested_by_joy_deletion_returns_when_no_target_orders() {
    Long joyId = 10L;
    LocalDateTime deletedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
    given(joyOrderRepository.findIdByJoyIdAndReservationFromAndPaymentStatusAndIsDeleted(
            joyId,
            deletedAt,
            JoyPaymentStatus.PAID,
            false
    )).willReturn(List.of());

    joyOrderService.setRefundRequestedByJoyDeletion(joyId, deletedAt);

    verify(joyOrderRepository, never()).updatePaymentStatusByJoyIdListAndStatus(any(), any());
    verify(joyOrderBatchService, never()).batchInsert(any());
}
```

- [ ] **단계 1.4: 양조장 삭제 환불 전환 테스트 추가**

동일 테스트 클래스에 다음 테스트를 추가한다.

```java
@Test
@SuppressWarnings("unchecked")
@DisplayName("양조장 삭제 환불 처리는 삭제 시점 이후 PAID 예약을 환불 요청 상태로 전환하고 이력을 저장한다")
void set_refund_requested_by_brewery_deletion_updates_future_paid_orders_and_inserts_histories() {
    Long breweryId = 20L;
    LocalDateTime deletedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
    given(joyOrderRepository.findIdByBreweryIdAndReservationFromAndPaymentStatusAndIsDeleted(
            breweryId,
            deletedAt,
            JoyPaymentStatus.PAID,
            false
    )).willReturn(List.of(3L, 4L));

    joyOrderService.setRefundRequestedByBreweryDeletion(breweryId, deletedAt);

    verify(joyOrderRepository).findIdByBreweryIdAndReservationFromAndPaymentStatusAndIsDeleted(
            breweryId,
            deletedAt,
            JoyPaymentStatus.PAID,
            false
    );
    verify(joyOrderRepository).updatePaymentStatusByJoyIdListAndStatus(
            List.of(3L, 4L),
            JoyPaymentStatus.REFUND_REQUESTED
    );
    ArgumentCaptor<List<JoyStatusHistoryBatchRow>> captor = ArgumentCaptor.forClass(List.class);
    verify(joyOrderBatchService).batchInsert(captor.capture());
    assertEquals(2, captor.getValue().size());
    assertEquals("양조장 삭제", captor.getValue().getFirst().getReasonCode());
}
```

- [ ] **단계 1.5: 테스트 실패 확인**

실행:

```bash
./gradlew test --tests '*JoyOrderServiceTest'
```

예상:

```text
컴파일 실패: JoyOrderService.setRefundRequestedByJoyDeletion, JoyOrderService.setRefundRequestedByBreweryDeletion,
JoyOrderRepository.findIdByBreweryIdAndReservationFromAndPaymentStatusAndIsDeleted 메서드가 아직 존재하지 않는다.
```

---

### 작업 2: 삭제 전용 환불 전환 구현

**파일:**
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyOrderRepository.java`
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`

- [ ] **단계 2.1: `JoyOrderRepository`에 양조장 삭제 대상 ID 조회 추가**

`JoyOrderRepository`의 기존 `findByBreweryIdAndReservationFromAndPaymentStatusAndIsDeleted` 아래에 다음 메서드를 추가한다.

```java
/**
 * 양조장 삭제 시 삭제 시점 이후 PAID 예약 식별자를 조회합니다.
 *
 * @param breweryId        양조장 식별자
 * @param reservationFrom  삭제 시점
 * @param joyPaymentStatus 환불 대상 결제 상태
 * @param isDeleted        예약 내역 삭제 여부
 * @return 환불 요청 대상 체험 예약 식별자 목록
 */
@Query("""
select jo.id from JoyOrder jo
join jo.joy j
where j.brewery.id = :breweryId
and jo.reservation >= :reservationFrom
and jo.joyPaymentStatus = :joyPaymentStatus
and jo.isDeleted = :isDeleted
""")
List<Long> findIdByBreweryIdAndReservationFromAndPaymentStatusAndIsDeleted(
        @Param("breweryId") Long breweryId,
        @Param("reservationFrom") LocalDateTime reservationFrom,
        @Param("joyPaymentStatus") JoyPaymentStatus joyPaymentStatus,
        @Param("isDeleted") Boolean isDeleted
);
```

- [ ] **단계 2.2: `JoyOrderService`에 삭제 전용 환불 전환 메서드 추가**

`JoyOrderService`의 `setRefundRequestedByJoyScheduleChange` 근처에 다음 메서드와 private helper를 추가한다.

```java
/**
 * 체험 삭제로 운영할 수 없어진 미래 PAID 예약을 환불 요청 상태로 전환합니다.
 *
 * @param joyId     삭제된 체험 식별자
 * @param deletedAt 삭제 처리 시각
 */
@Transactional
public void setRefundRequestedByJoyDeletion(Long joyId, LocalDateTime deletedAt) {
    List<Long> joyOrderIdList = joyOrderRepository.findIdByJoyIdAndReservationFromAndPaymentStatusAndIsDeleted(
            joyId,
            deletedAt,
            JoyPaymentStatus.PAID,
            false
    );
    setRefundRequestedForDeletedOwner(joyOrderIdList, "체험 삭제", "체험 삭제");
}

/**
 * 양조장 삭제로 운영할 수 없어진 미래 PAID 예약을 환불 요청 상태로 전환합니다.
 *
 * @param breweryId 삭제된 양조장 식별자
 * @param deletedAt 삭제 처리 시각
 */
@Transactional
public void setRefundRequestedByBreweryDeletion(Long breweryId, LocalDateTime deletedAt) {
    List<Long> joyOrderIdList = joyOrderRepository.findIdByBreweryIdAndReservationFromAndPaymentStatusAndIsDeleted(
            breweryId,
            deletedAt,
            JoyPaymentStatus.PAID,
            false
    );
    setRefundRequestedForDeletedOwner(joyOrderIdList, "양조장 삭제", "양조장 삭제");
}

private void setRefundRequestedForDeletedOwner(List<Long> joyOrderIdList, String reasonCode, String logPrefix) {
    if (joyOrderIdList.isEmpty()) {
        return;
    }

    // 환불 대상 예약 상태와 상태 이력을 같은 식별자 목록 기준으로 갱신한다.
    joyOrderRepository.updatePaymentStatusByJoyIdListAndStatus(joyOrderIdList, JoyPaymentStatus.REFUND_REQUESTED);
    int ret = joyOrderBatchService.batchInsert(
            joyOrderIdList.stream()
                    .map(id -> new JoyStatusHistoryBatchRow(id, JoyPaymentStatus.REFUND_REQUESTED, reasonCode))
                    .toList()
    );
    log.info("{}로 인한 JoyStatusHistory Batch Insert 건수: {}", logPrefix, ret);
}
```

- [ ] **단계 2.3: 삭제 전용 환불 전환 테스트 통과 확인**

실행:

```bash
./gradlew test --tests '*JoyOrderServiceTest'
```

예상:

```text
BUILD SUCCESSFUL
```

---

### 작업 3: 체험 삭제 API에 환불 전환 연결

**파일:**
- 수정: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`

- [ ] **단계 3.1: 체험 삭제 API 호출 테스트 추가**

`JoyServiceTest`의 삭제 관련 테스트 근처에 다음 테스트를 추가한다.

```java
@Test
@DisplayName("체험 삭제는 삭제 시점 이후 PAID 예약 환불 요청 처리를 호출한다")
void delete_joy_requests_refund_for_future_paid_orders() {
    Long userId = 1L;
    Long breweryId = 5L;
    Long joyId = 10L;
    Brewery brewery = mock(Brewery.class);
    Joy joy = mock(Joy.class);
    given(brewery.getId()).willReturn(breweryId);
    given(breweryRepository.findActiveByUserId(userId)).willReturn(Optional.of(brewery));
    given(joyRepository.findActiveByBreweryIdAndJoyId(breweryId, joyId)).willReturn(Optional.of(joy));

    joyService.deleteJoy(userId, joyId);

    verify(joy).setDeleted();
    verify(joyRepository).save(joy);
    verify(brewery).decreaseJoyCount();
    verify(joyOrderService).setRefundRequestedByJoyDeletion(eq(joyId), any(LocalDateTime.class));
}
```

`JoyServiceTest` import 영역에 `mock` static import가 없으면 추가한다.

```java
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
```

- [ ] **단계 3.2: 기존 이미 삭제된 체험 테스트에 no-op 검증 추가**

`delete_joy_rejects_already_deleted_joy` 마지막에 다음 검증을 추가한다.

```java
verify(joyOrderService, never()).setRefundRequestedByJoyDeletion(eq(joyId), any(LocalDateTime.class));
```

- [ ] **단계 3.3: 테스트 실패 확인**

실행:

```bash
./gradlew test --tests '*JoyServiceTest'
```

예상:

```text
테스트 실패: deleteJoy가 JoyOrderService.setRefundRequestedByJoyDeletion을 호출하지 않는다.
```

- [ ] **단계 3.4: `JoyService.deleteJoy`에 트랜잭션과 환불 전환 호출 추가**

`deleteJoy`를 다음 형태로 수정한다.

```java
/**
 * 삭제되지 않은 체험을 삭제 처리하고 삭제 시점 이후 PAID 예약을 환불 요청 대상으로 전환합니다.
 *
 * @param userId 체험을 관리하는 양조장 회원 식별자
 * @param joyId  삭제할 체험 식별자
 */
@Transactional
public void deleteJoy(Long userId, Long joyId) {
    Brewery brewery = breweryRepository.findActiveByUserId(userId).orElseThrow(() ->
            new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
    Joy joy = joyRepository.findActiveByBreweryIdAndJoyId(brewery.getId(), joyId).orElseThrow(() ->
            new ApplicationException(ApplicationError.JOY_NOT_FOUND));
    LocalDateTime deletedAt = LocalDateTime.now();
    joy.setDeleted();
    joyRepository.save(joy);
    brewery.decreaseJoyCount(); // 양조장의 체험 개수 카운트 1 감소
    joyOrderService.setRefundRequestedByJoyDeletion(joyId, deletedAt);
}
```

`JoyService` import 영역에 다음 항목이 없으면 추가한다.

```java
import java.time.LocalDateTime;
```

- [ ] **단계 3.5: 체험 삭제 테스트 통과 확인**

실행:

```bash
./gradlew test --tests '*JoyServiceTest'
```

예상:

```text
BUILD SUCCESSFUL
```

---

### 작업 4: 양조장 삭제 API에 환불 전환 연결

**파일:**
- 수정: `src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java`
- 수정: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`

- [ ] **단계 4.1: 필요한 import 추가**

`BreweryServiceTest` import 영역에 다음 항목이 없으면 추가한다.

```java
import com.example.monghyang.domain.auth.dto.VerifyAuthDto;
import com.example.monghyang.domain.users.entity.Users;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
```

- [ ] **단계 4.2: 양조장 삭제 API 호출 테스트 추가**

`BreweryServiceTest`에 다음 테스트를 추가한다.

```java
@Test
@DisplayName("양조장 탈퇴는 삭제 시점 이후 PAID 체험 예약 환불 요청 처리를 호출한다")
void brewery_quit_requests_refund_for_future_paid_orders() {
    Long userId = 1L;
    Long breweryId = 5L;
    VerifyAuthDto dto = new VerifyAuthDto();
    dto.setPassword("plain-password");
    Users users = mock(Users.class);
    Brewery brewery = mock(Brewery.class);
    given(users.getId()).willReturn(userId);
    given(users.getPassword()).willReturn("encoded-password");
    given(brewery.getId()).willReturn(breweryId);
    given(usersRepository.findById(userId)).willReturn(Optional.of(users));
    given(bCryptPasswordEncoder.matches(dto.getPassword(), "encoded-password")).willReturn(true);
    given(breweryRepository.findByUserId(userId)).willReturn(Optional.of(brewery));

    breweryService.breweryQuit(userId, dto);

    verify(brewery).setDeleted();
    verify(joyOrderService).setRefundRequestedByBreweryDeletion(eq(breweryId), any(LocalDateTime.class));
}
```

- [ ] **단계 4.3: 테스트 실패 확인**

실행:

```bash
./gradlew test --tests '*BreweryServiceTest'
```

예상:

```text
테스트 실패: breweryQuit가 JoyOrderService.setRefundRequestedByBreweryDeletion을 호출하지 않는다.
```

- [ ] **단계 4.4: `BreweryService.breweryQuit`에 환불 전환 호출 추가**

기존 한 줄 주석을 Javadocs로 바꾸고 메서드를 다음 형태로 수정한다.

```java
/**
 * 비밀번호 검증 후 양조장을 탈퇴 처리하고 삭제 시점 이후 PAID 체험 예약을 환불 요청 대상으로 전환합니다.
 *
 * @param userId         양조장 회원 식별자
 * @param quitRequestDto 비밀번호 검증 요청
 */
@Transactional
public void breweryQuit(Long userId, VerifyAuthDto quitRequestDto) {
    Users users = usersRepository.findById(userId).orElseThrow(() ->
            new ApplicationException(ApplicationError.USER_NOT_FOUND));
    if(!bCryptPasswordEncoder.matches(quitRequestDto.getPassword(), users.getPassword())) {
        throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
    }
    Brewery brewery = breweryRepository.findByUserId(users.getId()).orElseThrow(() ->
            new ApplicationException(ApplicationError.BREWERY_NOT_FOUND));
    LocalDateTime deletedAt = LocalDateTime.now();
    brewery.setDeleted();
    joyOrderService.setRefundRequestedByBreweryDeletion(brewery.getId(), deletedAt);
}
```

`BreweryService` import 영역에 다음 항목이 없으면 추가한다.

```java
import java.time.LocalDateTime;
```

- [ ] **단계 4.5: 양조장 삭제 테스트 통과 확인**

실행:

```bash
./gradlew test --tests '*BreweryServiceTest'
```

예상:

```text
BUILD SUCCESSFUL
```

---

### 작업 5: 전체 검증과 자체 검토

**파일:**
- 확인: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- 확인: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
- 확인: `src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java`

- [ ] **단계 5.1: 관련 테스트 전체 실행**

실행:

```bash
./gradlew test --tests '*JoyOrderServiceTest' --tests '*JoyServiceTest' --tests '*BreweryServiceTest'
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

- [ ] **단계 5.3: 자체 검토**

다음을 확인한다.

```text
삭제 API가 신규 예약 차단 정책을 바꾸지 않았는가?
환불 대상이 삭제 시점 이후, PAID, JoyOrder.isDeleted=false 예약으로 제한되는가?
대상 예약이 없을 때 빈 in 쿼리 또는 batch insert가 실행되지 않는가?
체험 삭제와 양조장 삭제가 상태 변경 및 환불 요청 전환을 같은 트랜잭션 안에서 수행하는가?
테스트 편의를 위해 production 코드에 Clock, setter, test-only constructor, visibility 완화가 추가되지 않았는가?
새 abstraction이나 새 dependency 없이 기존 repository/service/batch 흐름을 재사용했는가?
```

- [ ] **단계 5.4: 변경 범위 확인**

실행:

```bash
git diff -- src/main/java/com/example/monghyang/domain/joy/repository/JoyOrderRepository.java src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java src/main/java/com/example/monghyang/domain/joy/service/JoyService.java src/main/java/com/example/monghyang/domain/brewery/service/BreweryService.java src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java src/test/java/com/example/monghyang/domain/brewery/service/BreweryServiceTest.java
```

예상:

```text
이번 계획 범위 파일만 변경되어야 한다.
Javadocs, code comments, @DisplayName은 한국어여야 한다.
```

## 완료 보고 형식

구현 완료 후 보고에는 다음 항목을 포함한다.

```text
변경 파일:
- ...

구현 내용:
- ...

검증:
- ./gradlew test --tests '*JoyOrderServiceTest' --tests '*JoyServiceTest' --tests '*BreweryServiceTest'
- ./gradlew test

자체 검토:
- 삭제 시점 이후 PAID 예약만 환불 요청으로 전환됨
- 대상 없음 no-op 확인
- 테스트 편의용 production 설계 변경 없음

알려진 범위 밖:
- 후속 환불 배치 단계의 reason code 공통 문자열은 기존 정책 유지
```
