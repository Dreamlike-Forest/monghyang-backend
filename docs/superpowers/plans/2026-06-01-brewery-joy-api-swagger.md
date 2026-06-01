# Brewery Joy API Swagger Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 프론트엔드 작업자가 신규/변경 API 10개의 실제 요청/응답 계약을 Swagger에서 바로 확인할 수 있도록 문서를 보강합니다.

**Architecture:** 기존 API 동작은 변경하지 않고 컨트롤러 OpenAPI annotation과 요청/응답 DTO `@Schema` 설명만 보강합니다. Swagger 설명과 실제 구현이 다를 때는 실제 구현을 우선하며, 문서를 구현에 맞춥니다.

**Tech Stack:** Spring Boot 3.5.3, springdoc-openapi-starter-webmvc-ui 2.8.0, Swagger OpenAPI annotations.

---

### Task 1: 실제 계약 기준 정리

**Files:**
- Read: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`
- Read: `src/main/java/com/example/monghyang/domain/auth/controller/AuthController.java`
- Read: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryController.java`
- Read: `src/main/java/com/example/monghyang/domain/users/controller/UsersController.java`
- Read: related request/response DTO files

- [ ] **Step 1: 대상 API 계약 확인**

확인 대상:

```text
POST /api/brewery-priv/joy/schedule
POST /api/brewery-priv/brewery-close-try
POST /api/brewery-priv/brewery-close-confirmed
DELETE /api/brewery-priv/brewery-close
POST /api/brewery-priv/schedule
POST /api/auth/brewery-join
POST /api/brewery-priv/joy-add
POST /api/brewery-priv/update
GET /api/brewery/{breweryId}
GET /api/user/my
```

- [ ] **Step 2: 구현 우선 원칙 적용**

`brewery-close-try`는 실제 코드가 `ResponseDataDto<Void>` 성공 메시지만 반환하므로 Swagger에서 예약 목록 반환 설명을 제거합니다.

### Task 2: 컨트롤러 Swagger 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`
- Modify: `src/main/java/com/example/monghyang/domain/auth/controller/AuthController.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryController.java`
- Modify: `src/main/java/com/example/monghyang/domain/users/controller/UsersController.java`

- [ ] **Step 1: 내부 인증 파라미터 숨김**

`@LoginUserId`, `@LoginUserRole` 파라미터는 Swagger 요청 파라미터가 아니므로 아래 형식으로 숨깁니다.

```java
@Parameter(hidden = true) @LoginUserId Long userId
```

- [ ] **Step 2: 인증 필요 API에 security requirement 추가**

양조장 관리자 API와 내 정보 조회 API에는 세션 헤더 인증 요구를 명시합니다.

```java
@Operation(security = @SecurityRequirement(name = "SessionID"))
```

- [ ] **Step 3: 실제 성공/실패 응답 설명 추가**

`@ApiResponses`로 성공 응답, 인증 실패, 권한 실패, validation 실패, 주요 비즈니스 실패를 설명합니다. 실패 응답 스키마는 `ApplicationErrorDto`를 사용합니다.

### Task 3: 요청 DTO Schema 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/auth/dto/JoinDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/auth/dto/BreweryJoinDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/auth/dto/BreweryScheduleDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/dto/ReqJoyDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/dto/JoyScheduleDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/dto/ReqUpdateJoyScheduleDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/dto/ReqClosedDateTimeDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/dto/ReqUpdateBreweryScheduleDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/dto/ReqUpdateBreweryDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/image/dto/AddImageDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/image/dto/ModifySeqImageDto.java`

- [ ] **Step 1: 필드 의미, 필수 여부, 예시 추가**

`@Schema(description = "...", example = "...")` 형식으로 프론트 요청 작성에 필요한 정보를 추가합니다.

- [ ] **Step 2: 요일/날짜/시간 형식 명시**

요일은 `Mon`부터 `Sun`, 날짜는 `yyyy-MM-dd`, 시간은 `HH:mm:ss` 형식 예시로 설명합니다.

### Task 4: 응답 DTO Schema 보강

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/global/response/ResponseDataDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/global/advice/ApplicationErrorDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/dto/ResBreweryDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/brewery/dto/ResBreweryImageDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/joy/dto/ResJoyDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/users/dto/ResUsersPrivateInfoDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/users/dto/ResBreweryPrivateInfoDto.java`
- Modify: `src/main/java/com/example/monghyang/domain/users/dto/ResSellerPrivateInfoDto.java`

- [ ] **Step 1: 공통 성공/실패 응답 구조 설명**

`ResponseDataDto`와 `ApplicationErrorDto` 필드 설명을 추가합니다.

- [ ] **Step 2: 조회 응답 필드 설명**

양조장 상세 조회와 내 정보 조회에서 조건부로 내려오는 필드, 이미지 리스트, 체험 리스트, 상품 페이지 정보를 설명합니다.

### Task 5: 검증 및 자체 검토

**Files:**
- Verify: all modified Java source files

- [ ] **Step 1: 컴파일 검증**

Run:

```bash
./gradlew compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: 자체 검토**

검토 항목:

```text
실제 API 구현과 Swagger 설명이 충돌하지 않는가
내부 인증 파라미터가 Swagger 요청 필드로 노출되지 않는가
요청 DTO의 날짜/시간/요일/이미지 필드 예시가 충분한가
동작 변경 없이 문서만 보강했는가
불필요한 추상화나 범위 밖 리팩터링이 없는가
```

- [ ] **Step 3: 완료 보고**

변경 파일, 주요 보강 내용, 검증 결과, 남은 위험을 한국어로 보고합니다. 코드 변경 커밋은 사용자 확인 전 수행하지 않습니다.
