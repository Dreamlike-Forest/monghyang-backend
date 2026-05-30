# 체험 일정 스냅샷 기반 예약 검증 구현 계획

> **agentic worker 필수 하위 스킬:** 이 계획을 실행할 때는 `superpowers:subagent-driven-development` 또는 `superpowers:executing-plans`를 사용한다. 이 작업은 예약 검증, 환불 상태 전환, 쿼리 효율성, 인덱스 검토를 함께 포함하므로 기본 권장은 `superpowers:subagent-driven-development`이다. 각 단계는 체크박스(`- [ ]`)로 추적한다.

**목표:** 체험 생성, 체험 일정 변경, 예약 생성/변경이 모두 `JoyWeeklyStartTime` 스냅샷을 같은 기준으로 사용하도록 정합성을 맞추고, 일정 변경 이후 예약 환불 대상 조회 쿼리를 인덱스 친화적으로 개선한다.

**아키텍처:** 기존 양조장 스케줄 변경 흐름을 체험 스케줄에 맞게 좁게 확장한다. 새 계층을 만들지 않고 기존 `JoyService`, `JoyOrderService`, repository, controller에 필요한 책임만 추가한다. 예약 검증은 양조장 운영시간 스냅샷과 체험 시작 시간 스냅샷을 모두 통과해야 성공한다.

**기술 스택:** Java 21, Spring Boot, Spring Data JPA, Jakarta Bean Validation, Flyway, JUnit, Mockito. 정확한 라이브러리 버전과 Context7 library ID는 `docs/context7-dependencies.yaml` 복구 후 확정한다.

---

## 실행 전 차단 조건

- `docs/context7-dependencies.yaml`가 현재 저장소에 없다. Spring Boot, Spring Data JPA, Validation, JUnit 관련 구현 세부사항과 Context7 사용은 이 파일이 복구되거나 사용자가 대체 기준을 명시 승인하기 전까지 진행하지 않는다.
- `docs/junit-unit-test-guide.md`가 현재 저장소에 없다. 단위 테스트 작성은 이 문서가 복구되거나 사용자가 대체 기준을 명시 승인하기 전까지 진행하지 않는다.
- 이 문서는 구현 계획이며, production code 수정 승인이 아니다. 사용자가 이 계획 실행을 명시 승인해야 구현을 시작한다.
- 구현 시작 전 `superpowers:using-git-worktrees`를 적용한다. `main` 또는 `master`에서 바로 구현하지 않는다.

## 이 작업을 진행하는 이유

현재 체험 시작 시간 스냅샷(`JoyWeeklyStartTime`)은 체험 예약 캘린더 조회에는 반영되지만, 체험 생성과 예약 생성/변경 검증에는 일관되게 연결되어 있지 않다. 그 결과 사용자가 캘린더에서는 선택할 수 없는 체험 시작 시간으로 예약 생성 또는 예약 변경을 통과시킬 수 있다.

또한 양조장 운영시간 변경은 `effective_date` 기준 스냅샷 저장과 이후 `PAID` 예약 환불 전환을 수행하지만, 체험 자체의 요일별 시작 시간 변경에는 동일한 운영 메커니즘이 없다. 체험 일정이 바뀌어도 이미 잡힌 예약을 정책적으로 정리할 진입점이 부족하다.

따라서 체험 생성 시 최초 스냅샷을 만들고, 체험 일정 변경 시 새 스냅샷을 추가하며, 예약 생성/변경 시 해당 예약일에 활성화된 체험 시작 시간 스냅샷을 검증해야 한다.

## 기대 효과

- 체험 예약 캘린더, 신규 예약, 사용자 예약 변경, 양조장 관리자 예약 변경이 같은 스케줄 기준을 사용한다.
- 체험 일정 변경이 발생하면 적용일 당일 포함 이후의 `PAID` 예약을 `REFUND_REQUESTED`로 일괄 전환해 운영 정책을 명확히 한다.
- 환불 대상 조회에서 `date(reservation)` 같은 컬럼 함수 조건을 피하고 `reservation >= startDateTime` 범위 조건을 사용해 인덱스 활용 가능성을 높인다.
- 체험 시작 시간 스냅샷 조회와 환불 대상 조회에 필요한 보조 인덱스를 추가해 데이터 누적 시에도 조회 비용 증가를 줄인다.

## 파일 구조

- 생성: `src/main/java/com/example/monghyang/domain/joy/dto/JoyScheduleDto.java`
  - 체험 요일별 시작 시간 목록 요청 단위.
- 생성: `src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyScheduleDto.java`
  - 체험 일정 변경 요청 DTO.
- 수정: `src/main/java/com/example/monghyang/domain/joy/dto/ReqJoyDto.java`
  - 체험 생성 시 최초 스냅샷 생성을 위한 `schedules` 필드 추가.
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepository.java`
  - 동일 `effectiveDate` 삭제, 특정 예약일 활성 시작 시간 조회 추가.
- 수정: `src/main/java/com/example/monghyang/domain/joy/repository/JoyOrderRepository.java`
  - 특정 체험의 적용일 이후 `PAID` 예약 ID 조회 쿼리 추가. 기존 양조장 일정 변경 쿼리의 `date(reservation)` 사용은 별도 개선 대상으로 검토한다.
- 생성: `src/main/resources/db/migration/V<timestamp>__add_index_for_joy_order_refund_schedule.sql`
  - `joy_order` 환불 대상 조회와 환불 스케줄러 상태 조회를 위한 보조 인덱스 추가. `joy_weekly_start_time`은 기존 unique index를 사용한다.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
  - 체험 생성 시 최초 스냅샷 저장, 체험 일정 변경 처리 추가.
- 수정: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
  - 예약 검증에 체험 시작 시간 스냅샷 검증 추가, 체험 일정 변경 환불 처리 추가, 양조장 관리자 예약 변경의 기존 체험 ID 사용 오류 수정.
- 수정: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`
  - `/api/brewery-priv/joy/schedule` 엔드포인트 추가.
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`
  - 체험 생성 스냅샷, 체험 일정 변경 스냅샷/환불 트리거 검증.
- 테스트: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`
  - 예약 생성/변경 검증, 양조장 관리자 예약 변경 체험 ID 오류 회귀 검증.
- 테스트: `src/test/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepositoryTest.java`
  - 활성 스냅샷 조회 쿼리 검증.

---

### Task 0: 저장소 규칙 차단 해소

**Files:**
- 확인: `docs/context7-dependencies.yaml`
- 확인: `docs/junit-unit-test-guide.md`

- [ ] **Step 0.1: 의존성 기준 파일 존재 확인**

Run:

```bash
find docs -maxdepth 4 -type f
```

Expected:

```text
docs/context7-dependencies.yaml
docs/junit-unit-test-guide.md
```

- [ ] **Step 0.2: 파일이 없으면 구현 중단**

`docs/context7-dependencies.yaml` 또는 `docs/junit-unit-test-guide.md`가 없으면 사용자에게 다음 중 하나를 요청한다.

```text
Spring/JUnit 구현 세부사항을 확정하려면 저장소 규칙상 docs/context7-dependencies.yaml 및 docs/junit-unit-test-guide.md가 필요합니다. 두 파일을 복구할지, 또는 이번 작업에서 build.gradle을 임시 기준으로 사용할지 승인해주세요.
```

- [ ] **Step 0.3: Context7 사용 여부 결정**

정확한 Spring Data JPA annotation, Jakarta Validation 동작, JUnit 테스트 패턴 확인이 필요하면 `docs/context7-dependencies.yaml`의 `primary_library_id`를 기준으로 Context7을 1회 호출한다. 단순 저장소 내부 흐름 확인만으로 충분하면 Context7을 사용하지 않은 이유를 완료 보고에 남긴다.

### Task 1: 구현 작업 격리

**Files:**
- 확인: `.gitignore`
- 확인: `git status`

- [ ] **Step 1.1: 현재 브랜치와 작업 트리 확인**

Run:

```bash
git status --short
git branch --show-current
```

Expected:

```text
현재 작업 트리에 사용자 변경이 있으면 보존한다.
main 또는 master이면 별도 작업 브랜치 또는 worktree를 사용한다.
```

- [ ] **Step 1.2: `superpowers:using-git-worktrees` 적용**

구현 승인 후 `superpowers:using-git-worktrees`를 먼저 사용한다. 별도 worktree 생성이 필요한 경우 저장소 규칙과 권한 정책을 따른다.

### Task 2: DTO 입력 모델 정비

**Files:**
- Create: `src/main/java/com/example/monghyang/domain/joy/dto/JoyScheduleDto.java`
- Create: `src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyScheduleDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/dto/ReqJoyDto.java`

- [ ] **Step 2.1: 실패 테스트 작성**

`ReqJoyDto`와 `ReqUpdateJoyScheduleDto`가 `schedules` 누락, 빈 목록, `start_times` 빈 목록을 거부하는지 검증한다. 테스트 스타일은 `docs/junit-unit-test-guide.md`를 따른다.

Expected:

```text
구현 전에는 JoyScheduleDto 또는 ReqUpdateJoyScheduleDto가 없어 컴파일 실패한다.
```

- [ ] **Step 2.2: DTO 생성**

`JoyScheduleDto`는 `day_of_week`, `start_times`를 가진다. `start_times`는 `null`과 빈 목록을 모두 거부한다. DTO 필드 설명 주석은 한국어로 작성한다.

`ReqUpdateJoyScheduleDto`는 `joyId`, `effective_date`, `schedules`를 가진다. `effective_date`는 오늘 또는 미래만 허용한다. `schedules`는 `null`과 빈 목록을 모두 거부한다.

`ReqJoyDto`에는 체험 생성 시 최초 스냅샷을 만들기 위한 `schedules`를 추가한다.

- [ ] **Step 2.3: DTO 검증 테스트 실행**

Run:

```bash
./gradlew test --tests '*Joy*Dto*'
```

Expected:

```text
관련 DTO 검증 테스트 PASS
```

### Task 3: 스냅샷 조회와 환불 대상 조회 쿼리 정비

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepository.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/repository/JoyOrderRepository.java`
- Create: `src/main/resources/db/migration/V<timestamp>__add_index_for_joy_order_refund_schedule.sql`
- Test: `src/test/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepositoryTest.java`

- [ ] **Step 3.1: 활성 스냅샷 조회 실패 테스트 작성**

같은 체험, 같은 요일에 과거 `effectiveDate`와 미래 `effectiveDate`가 있을 때, 예약일 이하의 가장 큰 `effectiveDate` 그룹만 반환되는지 검증한다.

Expected:

```text
구현 전에는 findActiveStartTimesByJoyIdAndDate 메서드가 없어 컴파일 실패한다.
```

- [ ] **Step 3.2: `JoyWeeklyStartTimeRepository` 쿼리 추가**

추가할 책임:

- `deleteByJoyIdAndEffectiveDate(Long joyId, LocalDate effectiveDate)`
- `findActiveStartTimesByJoyIdAndDate(Long joyId, LocalDate targetDate, DayOfWeek dayOfWeek)`

효율성 기준:

- 삭제 쿼리는 기존 unique key `(joy_id, effective_date, day_of_week, start_time)`의 prefix를 활용한다.
- 활성 스냅샷 조회는 `joy_id`, `day_of_week`, `effective_date <= targetDate` 조건을 사용하되, 새 인덱스를 추가하지 않고 기존 unique key `(joy_id, effective_date, day_of_week, start_time)`를 활용한다.

- [ ] **Step 3.3: `JoyOrderRepository` 환불 대상 조회 추가**

추가할 책임:

- 특정 `joyId`의 `reservation >= effectiveDate.atStartOfDay()`
- `joyPaymentStatus = PAID`
- `isDeleted = false`
- 반환값은 상태 bulk update와 batch history insert에 사용할 예약 ID 목록

효율성 기준:

- `date(jo.reservation)`를 사용하지 않는다.
- `LocalDateTime` 범위 조건으로 인덱스 활용 가능성을 유지한다.

- [ ] **Step 3.4: Flyway 인덱스 마이그레이션 추가**

추가할 인덱스:

```sql
create index idx_joy_order_refund_schedule
    on joy_order (joy_payment_status, joy_id, is_deleted, reservation);
```

검토 기준:

- `joy_weekly_start_time`에는 새 인덱스를 추가하지 않는다. 기존 unique key `(joy_id, effective_date, day_of_week, start_time)`로 삭제 쿼리와 활성 스냅샷 조회를 처리한다.
- `idx_joy_order_refund_schedule`는 체험 일정 변경 환불 대상 조회에서 `joy_payment_status`, `joy_id`, `is_deleted` equality 조건 뒤 `reservation` range 조건을 지원한다.
- `idx_joy_order_refund_schedule`는 환불 스케줄러의 `joy_payment_status = REFUND_REQUESTED` 조회에도 선두 컬럼 prefix를 제공한다. 단, 스케줄러의 `createdAt ASC` 정렬까지 이 인덱스 하나로 최적화하지는 못하므로 스케줄러 정렬이 병목이면 별도 `(joy_payment_status, created_at)` 인덱스를 후속 검토한다.
- 운영 DB에 이미 같은 목적의 인덱스가 있으면 중복 생성하지 않는다.

- [ ] **Step 3.5: repository 테스트 실행**

Run:

```bash
./gradlew test --tests '*JoyWeeklyStartTimeRepositoryTest'
```

Expected:

```text
활성 스냅샷 조회 테스트 PASS
```

### Task 4: 체험 생성과 일정 변경 서비스 구현

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoyService.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepository.java`
- Test: `src/test/java/com/example/monghyang/domain/joy/service/JoyServiceTest.java`

- [ ] **Step 4.1: 체험 생성 최초 스냅샷 실패 테스트 작성**

`createJoy` 호출 시 `ReqJoyDto.schedules`에 담긴 요일별 시작 시간이 `LocalDate.now()` effectiveDate로 저장되는지 검증한다.

Expected:

```text
구현 전에는 스냅샷 저장 호출이 없어 검증 실패
```

- [ ] **Step 4.2: 체험 일정 변경 실패 테스트 작성**

`updateJoySchedule` 호출 시 같은 `effective_date`의 기존 스냅샷 삭제, 새 스냅샷 저장, 환불 요청 메서드 호출을 검증한다.

Expected:

```text
구현 전에는 updateJoySchedule 메서드가 없어 컴파일 실패
```

- [ ] **Step 4.3: 중복 입력 검증 구현**

서비스 레이어에서 다음 입력을 거부한다.

- 같은 요청 안의 중복 `day_of_week`
- 같은 요일 안의 중복 `start_time`
- `effective_date`가 과거인 요청
- 요청한 `joyId`가 로그인한 양조장 소유가 아닌 경우

기존 `ApplicationError`로 표현 가능한 오류를 우선 사용한다. 새 오류 enum은 기존 오류로 의미 전달이 불가능할 때만 추가한다.

- [ ] **Step 4.4: `createJoy` 스냅샷 저장 구현**

`Joy` 저장 후 생성된 엔티티를 사용해 `JoyWeeklyStartTime` 목록을 만든다. 별도 추상화나 helper class를 만들지 않고 `JoyService` 안에서 직접 저장한다. 여러 건 저장에는 repository의 bulk 저장 메서드를 사용한다.

- [ ] **Step 4.5: `updateJoySchedule` 구현**

처리 순서:

1. 로그인 사용자의 양조장 조회
2. `joyId`가 해당 양조장 소유인지 확인
3. `effective_date` 과거 여부 검증
4. 요청 내부 중복 검증
5. 같은 `effective_date` 기존 스냅샷 삭제
6. 새 스냅샷 저장
7. `joyOrderService.setRefundRequestedByJoyScheduleChange(joyId, effectiveDate)` 호출

- [ ] **Step 4.6: 서비스 테스트 실행**

Run:

```bash
./gradlew test --tests '*JoyServiceTest'
```

Expected:

```text
체험 생성 스냅샷 테스트 PASS
체험 일정 변경 스냅샷/환불 트리거 테스트 PASS
```

### Task 5: 예약 검증과 환불 상태 전환 구현

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/joy/service/JoyOrderService.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/repository/JoyOrderRepository.java`
- Test: `src/test/java/com/example/monghyang/domain/joy/service/JoyOrderServiceTest.java`

- [ ] **Step 5.1: 스냅샷 밖 시간 예약 실패 테스트 작성**

예약일에 활성화된 `JoyWeeklyStartTime` 목록에 없는 `reservation_time`으로 예약 슬롯 증가를 시도하면 `JOY_ORDER_TIME_INVALID`가 발생해야 한다.

Expected:

```text
구현 전에는 양조장 운영시간과 timeUnit만 맞으면 통과하므로 테스트 실패
```

- [ ] **Step 5.2: 사용자 예약 변경 실패 테스트 작성**

사용자 예약 변경 시 새 예약 시간이 활성 체험 시작 시간 스냅샷에 없으면 `JOY_ORDER_TIME_INVALID`가 발생해야 한다.

Expected:

```text
구현 전에는 체험 시작 시간 스냅샷 검증이 없어 테스트 실패
```

- [ ] **Step 5.3: 양조장 관리자 예약 변경 회귀 테스트 작성**

`updateReservationByBrewery`가 예약 ID가 아니라 `joyOrder.getJoy().getId()`로 슬롯을 증가시키는지 검증한다. 기존 예약 슬롯 감소는 기존 예약 일시와 인원 기준으로 수행해야 한다.

Expected:

```text
구현 전에는 잘못된 ID 또는 기존 예약 감소 기준 때문에 테스트 실패
```

- [ ] **Step 5.4: `verifyReservation` 시그니처 정리**

`JoyInfoDto`에는 `joyId`가 없으므로 `joyInfoDto.joyId()`를 사용하지 않는다. 가장 작은 변경은 `verifyReservation(Long joyId, JoyInfoDto joyInfoDto, LocalDate reservationDate, LocalTime reservationTime, Integer count)` 형태로 `joyId`를 별도 전달하는 것이다.

- [ ] **Step 5.5: 체험 시작 시간 스냅샷 검증 추가**

검증 순서:

1. 최소/최대 인원 검증
2. 예약 시간이 현재보다 과거인지 검증
3. 양조장 운영시간 범위와 `timeUnit` 간격 검증
4. 예약일의 `DayOfWeek` 계산
5. `JoyWeeklyStartTimeRepository.findActiveStartTimesByJoyIdAndDate` 조회
6. 조회 결과 중 `reservationTime`과 같은 `startTime`이 없으면 `JOY_ORDER_TIME_INVALID`

- [ ] **Step 5.6: 체험 일정 변경 환불 처리 구현**

`setRefundRequestedByJoyScheduleChange(Long joyId, LocalDate effectiveDate)`를 추가한다.

처리 기준:

- `effectiveDate.atStartOfDay()` 이후 예약
- 해당 `joyId`
- `PAID`
- `isDeleted = false`
- 대상이 없으면 상태 변경과 이력 insert를 수행하지 않고 반환
- 대상이 있으면 상태를 `REFUND_REQUESTED`로 bulk update
- `JoyOrderBatchService.batchInsert`로 `JoyStatusHistoryBatchRow`를 일괄 저장
- reason code는 기존 관례에 맞춰 한국어 문자열을 사용한다.

- [ ] **Step 5.7: `updateReservationByBrewery` 기존 오류 수정**

수정 기준:

- 새 슬롯 증가에는 `joyOrder.getJoy().getId()`를 사용한다.
- 기존 슬롯 감소에는 변경 전 `joyOrder.getReservation().toLocalDate()`, `joyOrder.getReservation().toLocalTime()`, `joyOrder.getCount()`를 사용한다.
- 사용자 변경과 동일하게 새 예약 시간 검증을 수행한다.

- [ ] **Step 5.8: 예약 서비스 테스트 실행**

Run:

```bash
./gradlew test --tests '*JoyOrderServiceTest'
```

Expected:

```text
스냅샷 밖 시간 예약 차단 테스트 PASS
사용자 예약 변경 차단 테스트 PASS
양조장 관리자 예약 변경 회귀 테스트 PASS
체험 일정 변경 환불 상태 전환 테스트 PASS
```

### Task 6: 양조장 관리자 API 연결

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`

- [ ] **Step 6.1: controller 연결 실패 테스트 또는 컴파일 검증 준비**

현재 controller 계층 테스트 구조가 없으면 과도한 새 테스트 인프라를 만들지 않는다. 서비스 테스트와 `compileJava`로 메서드 바인딩을 1차 검증한다.

- [ ] **Step 6.2: `/api/brewery-priv/joy/schedule` 엔드포인트 추가**

요청 형식:

- `@LoginUserId Long userId`
- `@Valid @ModelAttribute ReqUpdateJoyScheduleDto dto`

처리:

- `joyService.updateJoySchedule(userId, dto)` 호출
- 성공 응답은 기존 `ResponseDataDto.success` 패턴을 따른다.

- [ ] **Step 6.3: 컴파일 검증**

Run:

```bash
./gradlew compileJava
```

Expected:

```text
BUILD SUCCESSFUL
```

### Task 7: 쿼리 효율성 검증

**Files:**
- Review: `src/main/java/com/example/monghyang/domain/joy/repository/JoyWeeklyStartTimeRepository.java`
- Review: `src/main/java/com/example/monghyang/domain/joy/repository/JoyOrderRepository.java`
- Review: `src/main/resources/db/migration/V<timestamp>__add_index_for_joy_order_refund_schedule.sql`

- [ ] **Step 7.1: 컬럼 함수 사용 여부 확인**

Run:

```bash
rg -n "date\\(jo\\.reservation\\)|date\\(.*reservation" src/main/java/com/example/monghyang/domain/joy src/main/java/com/example/monghyang/domain/brewery
```

Expected:

```text
새로 추가한 체험 일정 변경 환불 대상 조회에는 date(jo.reservation)가 없어야 한다.
기존 양조장 일정 변경 쿼리에 남아 있으면 별도 개선 후보로 보고한다.
```

- [ ] **Step 7.2: 인덱스와 쿼리 조건 순서 검토**

검토 기준:

- `joy_weekly_start_time`: 새 인덱스를 추가하지 않고 기존 unique key `(joy_id, effective_date, day_of_week, start_time)` 사용. `effective_date` range 이후 `day_of_week`가 완전한 탐색 조건이 아닐 수 있으나, 체험별 스냅샷 규모가 작다는 전제로 허용한다.
- `joy_order`: `joy_payment_status`, `joy_id`, `is_deleted` equality 후 `reservation` range
- 환불 스케줄러: 같은 `joy_order` 인덱스의 선두 `joy_payment_status` prefix를 사용할 수 있는지 확인. `created_at` 정렬 최적화가 필요한 경우 별도 인덱스 필요성을 보고한다.

Expected:

```text
새 쿼리의 where 조건이 추가 인덱스의 선두 컬럼과 맞는다.
```

- [ ] **Step 7.3: SQL 로그 또는 DB 실행 계획 검증**

로컬 DB가 준비되어 있으면 환불 대상 조회 SQL에 대해 실행 계획을 확인한다. 로컬 DB가 없으면 정적 검토 결과와 인덱스 근거를 완료 보고에 남긴다.

Expected:

```text
reservation 컬럼에 함수가 적용되지 않고 range 조건으로 전달된다.
```

### Task 8: 전체 검증과 자체 리뷰

**Files:**
- Review: 변경된 전체 파일

- [ ] **Step 8.1: 전체 컴파일**

Run:

```bash
./gradlew compileJava
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 8.2: 전체 테스트**

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 8.3: 자체 리뷰**

확인 항목:

- 새 class는 DTO 2개와 마이그레이션 1개로 제한했는가
- `JoyInfoDto`에 불필요한 `joyId`를 추가하지 않고 서비스 메서드 인자로 해결했는가
- 테스트 편의를 위해 production code 가시성이나 생성자를 부자연스럽게 바꾸지 않았는가
- 기존 양조장 스케줄 변경 흐름을 불필요하게 리팩터링하지 않았는가
- 환불 대상 조회에서 `date(reservation)`를 새 쿼리에 사용하지 않았는가
- 양조장 관리자 예약 변경에서 잘못된 ID 사용과 기존 슬롯 감소 기준 오류를 같이 해결했는가

- [ ] **Step 8.4: 완료 보고**

보고 내용:

- 변경 파일 목록
- 구현된 동작
- 실행한 검증 명령과 결과
- 쿼리 효율성 검증 결과
- `docs/context7-dependencies.yaml`, `docs/junit-unit-test-guide.md` 처리 방식
- 알려진 리스크와 후속 개선 후보

## 범위 제외

- 기존 양조장 일정 변경 쿼리의 `date(jo.reservation)` 제거는 이번 작업에서 직접 필요한 범위를 넘을 수 있으므로 기본 구현 범위에서 제외한다. 다만 Task 7에서 발견 결과를 보고한다.
- 체험 별도 휴무일, 체험 별도 휴무 시간대 정책은 이번 계획에서 변경하지 않는다.
- 환불 PG 연동 방식과 스케줄러 동작은 기존 흐름을 유지한다.
- 새로운 공통 schedule abstraction은 만들지 않는다. 현재 요구사항은 체험 스케줄에 한정되므로 기존 서비스에 직접 구현한다.

## 계획 자체 리뷰

- 기존 계획의 컴파일 오류 가능성인 `joyInfoDto.joyId()` 사용을 제거하고, `verifyReservation`에 `joyId`를 별도 전달하는 방향으로 수정했다.
- 기존 계획에 없던 `updateReservationByBrewery`의 체험 ID 사용 오류와 기존 슬롯 감소 기준 오류를 작업 범위에 포함했다. 이는 예약 변경 시 스냅샷 검증을 넣는 과정에서 같은 메서드를 수정해야 하므로 범위 내 결함 수정이다.
- 환불 대상 조회는 `date(reservation)` 대신 `LocalDateTime` range 조건을 사용하도록 명시했다.
- `joy_weekly_start_time`은 새 인덱스를 추가하지 않고 기존 unique key를 사용하도록 조정했다. `joy_order` 인덱스는 체험 일정 변경 환불 대상 조회와 환불 스케줄러의 상태 조회가 같은 선두 컬럼을 공유하도록 `(joy_payment_status, joy_id, is_deleted, reservation)` 순서로 유지한다.
- `docs/context7-dependencies.yaml`와 `docs/junit-unit-test-guide.md` 부재를 실행 전 차단 조건으로 명시해 저장소 규칙 위반 없이 구현을 시작할 수 있도록 했다.

## 의존성 및 Context7 기록

- version: `blocked` (`docs/context7-dependencies.yaml` 부재로 확정 불가)
- source: `docs/context7-dependencies.yaml`
- context7_library_id: `not_used` (`docs/context7-dependencies.yaml`가 없어 공식 library ID를 확정할 수 없고, 현재 단계는 구현이 아닌 계획 개편이다.)
