# 감사 및 운영 오류 로깅 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 인증/인가 및 중요 계정 이벤트와 양조장/판매자 쓰기 작업 결과는 성공/실패 모두 감사 로그로 남기고, 일반 API 정상/예상 경로는 응답 로그를 남기지 않으며, 5xx와 외부 연동 실패 같은 운영상 오류는 API 종류와 무관하게 전량 기록한다.

**Architecture:** 기존 `LoggingFilter`의 전량 `RES_RESULT` 로그를 제거하고 5xx 미기록 오류의 보조 안전망으로 축소한다. 새 `AuditLogger`가 MDC 기반 구조화 필드를 한 곳에서 관리하며, 보안 핸들러, 도메인 쓰기 결과 필터, 전역 예외 처리기는 이 컴포넌트를 통해 감사 로그와 운영 오류 로그를 남긴다.

**Tech Stack:** Java 21, Spring Boot 3.5.3, Spring Security 6.5.1, SLF4J/Logback, logstash-logback-encoder 7.4, JUnit 5, Mockito

---

## 기준 문서 및 제약

- 의존성 기준: `docs/context7-dependencies.yaml`
- 단위 테스트 기준: `docs/junit-unit-test-guide.md`
- Context7 확인: `logstash-logback-encoder 7.4` 기준으로 `LoggingEventCompositeJsonEncoder`의 `<message/>`, `<logLevel/>`, `<mdc/>`, `<arguments/>`, `<stackTrace/>` provider 사용 가능성을 확인했다.
- 코드 주석, Javadocs, 테스트 `@DisplayName`은 한국어로 작성한다.
- 토큰, 비밀번호, 쿠키, 세션 ID, refresh token 원문은 로그에 남기지 않는다.
- 코드베이스 변경 커밋은 사용자 확인 전까지 수행하지 않는다. 이 규칙은 `superpowers:writing-plans`의 일반적인 "frequent commits" 권장보다 우선한다.

## 로그 정책

### 전량 기록 대상

- `LOGIN_SUCCESS`, `LOGIN_FAILURE`
- `OAUTH2_LOGIN_SUCCESS`, `OAUTH2_LOGIN_INCOMPLETE`, `OAUTH2_LOGIN_FAILURE`
- `LOGOUT_SUCCESS`, `LOGOUT_FAILURE`
- `TOKEN_REFRESH_SUCCESS`, `TOKEN_REFRESH_FAILURE`
- `AUTHENTICATION_REQUIRED`, `AUTHORIZATION_DENIED`
- `PASSWORD_RESET_SUCCESS`, `PASSWORD_VERIFY_SUCCESS`, `PASSWORD_VERIFY_FAILURE`
- `USER_UPDATE_SUCCESS`, `USER_WITHDRAWAL_SUCCESS`
- `BREWERY_WRITE_RESULT`, `SELLER_WRITE_RESULT`: `/api/brewery-priv/**`, `/api/seller-priv/**`의 `POST`, `PUT`, `PATCH`, `DELETE` 결과를 성공/실패 모두 기록한다. `GET`, `HEAD`, `OPTIONS`는 조회/사전 요청으로 보고 제외한다.
- 현재 코드베이스에는 별도 관리자 기능 실행 핸들러나 역할 변경 API가 확인되지 않았으므로 `ADMIN_ACTION`, `ROLE_CHANGE`는 이번 구현 범위에 추가하지 않는다.
- 5xx, 미처리 예외, 트랜잭션 실패, 데이터 정합성 오류, AWS S3/Redis/PG 같은 외부 연동 실패

### 기본 기록 제외 대상

- 일반 조회 성공
- 검색 결과 없음
- 일반 2xx/3xx 응답
- 예상 가능한 비즈니스 4xx
- DTO 검증 실패, 타입 불일치 같은 일반 사용자 입력 오류

## 변경 파일 구조

- Create: `src/main/java/com/example/monghyang/domain/logging/AuditLogger.java`
  - 감사 로그와 운영 오류 로그의 MDC 필드, 이벤트 이름, 중복 방지 request attribute를 관리한다.
- Modify: `src/main/java/com/example/monghyang/domain/logging/LoggingFilter.java`
  - 모든 응답에 대한 `RES_RESULT` 로그를 제거하고, 중앙 오류 로그가 누락된 5xx만 `RES_ERROR`로 남긴다.
- Create: `src/main/java/com/example/monghyang/domain/logging/DomainWriteLoggingFilter.java`
  - 양조장/판매자 전용 쓰기 경로의 응답 결과를 성공/실패 모두 도메인 감사 로그로 남긴다.
- Modify: `src/main/resources/logback-spring.xml`
  - JSON 파일 로그에 `severity`, `message` provider를 추가하고 MDC와 stack trace를 유지한다.
- Modify: `src/main/java/com/example/monghyang/domain/global/advice/GlobalExceptionHandler.java`
  - 일반 비즈니스 4xx는 무로그로 두고, 운영상 오류만 `AuditLogger`로 기록한다.
- Modify: `src/main/java/com/example/monghyang/domain/util/SecurityFilterExceptionResponseWriter.java`
  - 직접 MDC를 쓰는 책임을 제거하고 응답 작성 책임만 유지한다.
- Modify: `src/main/java/com/example/monghyang/domain/security/authHandler/*.java`
  - 폼 로그인, 로그아웃, 인증 필요, 접근 거부 이벤트를 감사 로그로 남긴다.
- Modify: `src/main/java/com/example/monghyang/domain/oauth2/handler/*.java`
  - OAuth2 성공/미완료/실패 이벤트를 감사 로그로 남긴다.
- Modify: `src/main/java/com/example/monghyang/domain/auth/service/AuthService.java`
  - 토큰 갱신, 비밀번호 초기화, 비밀번호 검증 이벤트를 감사 로그로 남긴다.
- Modify: `src/main/java/com/example/monghyang/domain/users/controller/UsersController.java`
  - 회원 정보 수정과 탈퇴 성공 이벤트를 감사 로그로 남긴다.
- Modify: `src/test/java/com/example/monghyang/devtest/AuthServiceTest.java`
  - `AuthService` 생성자와 비밀번호 관련 메서드 시그니처 변경을 반영한다.
- Modify: `src/main/java/com/example/monghyang/domain/util/JwtUtil.java`
  - 토큰 훼손 로컬 로그를 제거하고 cause를 보존한 `ApplicationException`으로 위임한다.
- Modify: `src/main/java/com/example/monghyang/domain/image/service/impl/AwsStorageService.java`
  - AWS S3 실패의 로컬 중복 로그를 제거하고 cause를 보존한 `ApplicationException`으로 위임한다.
- Test: `src/test/java/com/example/monghyang/domain/logging/AuditLoggerTest.java`
- Test: `src/test/java/com/example/monghyang/domain/logging/LoggingFilterTest.java`
- Test: `src/test/java/com/example/monghyang/domain/logging/DomainWriteLoggingFilterTest.java`
- Test: `src/test/java/com/example/monghyang/domain/global/advice/GlobalExceptionHandlerTest.java`
- Test: `src/test/java/com/example/monghyang/domain/security/authHandler/SessionLoginSuccessHandlerTest.java`
- Test: `src/test/java/com/example/monghyang/domain/security/authHandler/SessionLoginFailureHandlerTest.java`
- Test: `src/test/java/com/example/monghyang/domain/auth/service/AuthServiceAuditLogTest.java`

## 복잡도 정당화

새 공통 컴포넌트로 `AuditLogger`를 추가한다. 인증/인가 핸들러, 도메인 쓰기 결과 필터, 전역 예외 처리, 계정 관련 서비스가 모두 로그를 남겨야 하므로 호출부마다 MDC 필드명을 직접 관리하면 필드 누락과 민감정보 유출 위험이 커진다. 양조장/판매자 쓰기 결과는 경로와 HTTP method로 명확히 식별되므로 `DomainWriteLoggingFilter` 하나로 처리해 컨트롤러별 반복 수정을 피한다. 별도 인터페이스, enum 카탈로그, AOP, annotation 기반 추상화는 현재 요구보다 크므로 도입하지 않는다.

---

### Task 1: 감사 로그 공통 컴포넌트 추가

**Files:**
- Create: `src/main/java/com/example/monghyang/domain/logging/AuditLogger.java`
- Test: `src/test/java/com/example/monghyang/domain/logging/AuditLoggerTest.java`

- [ ] **Step 1: 실패하는 단위 테스트 작성**

`src/test/java/com/example/monghyang/domain/logging/AuditLoggerTest.java`를 생성한다.

```java
package com.example.monghyang.domain.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLoggerTest {
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(AuditLogger.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        auditLogger = new AuditLogger();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("보안 성공 감사 로그는 이벤트명, 결과, 사용자, 요청 정보를 MDC에 포함한다")
    void log_security_success_writes_structured_mdc() {
        MockHttpServletRequest request = request("/api/auth/login", "POST");

        auditLogger.logSecuritySuccess("LOGIN_SUCCESS", request, 1L, "ROLE_USER");

        ILoggingEvent event = appender.list.get(0);
        Map<String, String> mdc = event.getMDCPropertyMap();
        assertEquals("SECURITY_AUDIT", event.getFormattedMessage());
        assertEquals("SECURITY", mdc.get("eventCategory"));
        assertEquals("LOGIN_SUCCESS", mdc.get("eventName"));
        assertEquals("SUCCESS", mdc.get("eventOutcome"));
        assertEquals("1", mdc.get("userId"));
        assertEquals("ROLE_USER", mdc.get("userRole"));
        assertEquals("/api/auth/login", mdc.get("requestURI"));
        assertEquals("POST", mdc.get("method"));
        assertTrue(Boolean.TRUE.equals(request.getAttribute(AuditLogger.SECURITY_EVENT_LOGGED_ATTRIBUTE)));
    }

    @Test
    @DisplayName("운영 오류 로그는 중복 방지 속성과 오류 코드를 남긴다")
    void log_operational_error_marks_request_attribute() {
        MockHttpServletRequest request = request("/api/images/1", "GET");
        RuntimeException cause = new RuntimeException("s3 timeout");

        auditLogger.logOperationalError("AWS_S3_LOAD_ERROR", request, "IMAGE_LOAD_ERROR", 500, cause);

        ILoggingEvent event = appender.list.get(0);
        Map<String, String> mdc = event.getMDCPropertyMap();
        assertEquals("OPERATIONAL_ERROR", event.getFormattedMessage());
        assertEquals("OPERATIONAL", mdc.get("eventCategory"));
        assertEquals("AWS_S3_LOAD_ERROR", mdc.get("eventName"));
        assertEquals("FAILURE", mdc.get("eventOutcome"));
        assertEquals("IMAGE_LOAD_ERROR", mdc.get("errorName"));
        assertEquals("500", mdc.get("httpStatus"));
        assertTrue(Boolean.TRUE.equals(request.getAttribute(AuditLogger.OPERATIONAL_ERROR_LOGGED_ATTRIBUTE)));
    }

    @Test
    @DisplayName("도메인 쓰기 결과 로그는 소유자 유형, 응답 상태, 처리 시간을 MDC에 포함한다")
    void log_domain_write_result_writes_structured_mdc() {
        MockHttpServletRequest request = request("/api/brewery-priv/joy-add", "POST");

        auditLogger.logDomainWriteResult("BREWERY", request, 200, 15L);

        ILoggingEvent event = appender.list.get(0);
        Map<String, String> mdc = event.getMDCPropertyMap();
        assertEquals("DOMAIN_WRITE_RESULT", event.getFormattedMessage());
        assertEquals("DOMAIN_WRITE", mdc.get("eventCategory"));
        assertEquals("BREWERY_WRITE_RESULT", mdc.get("eventName"));
        assertEquals("SUCCESS", mdc.get("eventOutcome"));
        assertEquals("BREWERY", mdc.get("ownerType"));
        assertEquals("200", mdc.get("httpStatus"));
        assertEquals("15", mdc.get("durationMs"));
        assertTrue(Boolean.TRUE.equals(request.getAttribute(AuditLogger.DOMAIN_WRITE_LOGGED_ATTRIBUTE)));
    }

    private MockHttpServletRequest request(String uri, String method) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.logging.AuditLoggerTest`

Expected: `AuditLogger` 클래스가 없어 컴파일 실패한다.

- [ ] **Step 3: 최소 구현 작성**

`src/main/java/com/example/monghyang/domain/logging/AuditLogger.java`를 생성한다.

```java
package com.example.monghyang.domain.logging;

import com.example.monghyang.domain.global.advice.ApplicationError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 감사 로그와 운영 오류 로그의 구조화 필드를 일관되게 기록합니다.
 *
 * <p>토큰, 비밀번호, 쿠키, 세션 식별자 같은 민감값은 기록하지 않습니다.
 * 요청 단위 중복 로그를 줄이기 위해 request attribute에 기록 여부를 표시합니다.</p>
 */
@Component
@Slf4j
public class AuditLogger {
    public static final String SECURITY_EVENT_LOGGED_ATTRIBUTE = AuditLogger.class.getName() + ".securityEventLogged";
    public static final String OPERATIONAL_ERROR_LOGGED_ATTRIBUTE = AuditLogger.class.getName() + ".operationalErrorLogged";
    public static final String DOMAIN_WRITE_LOGGED_ATTRIBUTE = AuditLogger.class.getName() + ".domainWriteLogged";

    /**
     * 인증/인가 또는 중요 계정 작업의 성공 이벤트를 감사 로그로 기록합니다.
     *
     * @param eventName 감사 이벤트 이름
     * @param request 현재 요청
     * @param userId 회원 식별자
     * @param userRole 회원 권한
     */
    public void logSecuritySuccess(String eventName, HttpServletRequest request, Long userId, String userRole) {
        Map<String, String> fields = baseFields("SECURITY", eventName, "SUCCESS", request);
        putIfNotNull(fields, "userId", userId);
        putIfNotBlank(fields, "userRole", userRole);
        mark(request, SECURITY_EVENT_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> log.info("SECURITY_AUDIT"));
    }

    /**
     * 인증/인가 또는 중요 계정 작업의 실패 이벤트를 감사 로그로 기록합니다.
     *
     * @param eventName 감사 이벤트 이름
     * @param request 현재 요청
     * @param userId 확인된 회원 식별자. 알 수 없으면 null
     * @param userRole 확인된 회원 권한. 알 수 없으면 null
     * @param error 실패 원인으로 매핑된 애플리케이션 오류
     */
    public void logSecurityFailure(String eventName, HttpServletRequest request, Long userId, String userRole, ApplicationError error) {
        Map<String, String> fields = baseFields("SECURITY", eventName, "FAILURE", request);
        putIfNotNull(fields, "userId", userId);
        putIfNotBlank(fields, "userRole", userRole);
        if (error != null) {
            fields.put("errorName", error.name());
            fields.put("httpStatus", String.valueOf(error.getStatus().value()));
        }
        mark(request, SECURITY_EVENT_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> log.warn("SECURITY_AUDIT"));
    }

    /**
     * 운영자가 조사해야 하는 서버 오류 또는 외부 연동 실패를 기록합니다.
     *
     * @param eventName 운영 오류 이벤트 이름
     * @param request 현재 요청
     * @param errorName 오류 코드 또는 예외 분류명
     * @param httpStatus 응답 HTTP 상태 코드
     * @param throwable 원본 예외
     */
    public void logOperationalError(String eventName, HttpServletRequest request, String errorName, int httpStatus, Throwable throwable) {
        Map<String, String> fields = baseFields("OPERATIONAL", eventName, "FAILURE", request);
        putIfNotBlank(fields, "errorName", errorName);
        fields.put("httpStatus", String.valueOf(httpStatus));
        mark(request, OPERATIONAL_ERROR_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> log.error("OPERATIONAL_ERROR", throwable));
    }

    /**
     * 양조장 또는 판매자 쓰기 작업의 응답 결과를 감사 로그로 기록합니다.
     *
     * @param ownerType 작업 주체 유형. BREWERY 또는 SELLER
     * @param request 현재 요청
     * @param httpStatus 응답 HTTP 상태 코드
     * @param durationMs 요청 처리 시간
     */
    public void logDomainWriteResult(String ownerType, HttpServletRequest request, int httpStatus, long durationMs) {
        String normalizedOwnerType = ownerType == null ? "UNKNOWN" : ownerType;
        Map<String, String> fields = baseFields("DOMAIN_WRITE", normalizedOwnerType + "_WRITE_RESULT", outcomeOf(httpStatus), request);
        fields.put("ownerType", normalizedOwnerType);
        fields.put("httpStatus", String.valueOf(httpStatus));
        fields.put("durationMs", String.valueOf(durationMs));
        mark(request, DOMAIN_WRITE_LOGGED_ATTRIBUTE);
        withMdc(fields, () -> {
            if (httpStatus >= 500) {
                log.error("DOMAIN_WRITE_RESULT");
            } else if (httpStatus >= 400) {
                log.warn("DOMAIN_WRITE_RESULT");
            } else {
                log.info("DOMAIN_WRITE_RESULT");
            }
        });
    }

    private Map<String, String> baseFields(String category, String eventName, String outcome, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("eventCategory", category);
        fields.put("eventName", eventName);
        fields.put("eventOutcome", outcome);
        if (request != null) {
            putIfNotBlank(fields, "requestURI", request.getRequestURI());
            putIfNotBlank(fields, "method", request.getMethod());
            putIfNotBlank(fields, "clientIp", request.getRemoteAddr());
        }
        return fields;
    }

    private void withMdc(Map<String, String> fields, Runnable logAction) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            fields.forEach(MDC::put);
            logAction.run();
        } finally {
            if (previous == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(previous);
            }
        }
    }

    private void mark(HttpServletRequest request, String attributeName) {
        if (request != null) {
            request.setAttribute(attributeName, Boolean.TRUE);
        }
    }

    private void putIfNotNull(Map<String, String> fields, String key, Object value) {
        if (value != null) {
            fields.put(key, String.valueOf(value));
        }
    }

    private void putIfNotBlank(Map<String, String> fields, String key, String value) {
        if (value != null && !value.isBlank()) {
            fields.put(key, value);
        }
    }

    private String outcomeOf(int httpStatus) {
        return httpStatus >= 400 ? "FAILURE" : "SUCCESS";
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.logging.AuditLoggerTest`

Expected: `BUILD SUCCESSFUL`

---

### Task 2: 전량 응답 로그 제거 및 5xx 안전망 유지

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/logging/LoggingFilter.java`
- Modify: `src/main/resources/logback-spring.xml`
- Test: `src/test/java/com/example/monghyang/domain/logging/LoggingFilterTest.java`

- [ ] **Step 1: 실패하는 필터 테스트 작성**

`src/test/java/com/example/monghyang/domain/logging/LoggingFilterTest.java`를 생성한다.

```java
package com.example.monghyang.domain.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingFilterTest {
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private LoggingFilter filter;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        filter = new LoggingFilter();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("정상 응답은 공통 응답 로그를 남기지 않는다")
    void successful_response_does_not_write_response_log() throws Exception {
        executeWithStatus(HttpServletResponse.SC_OK);

        assertEquals(0, appender.list.size());
    }

    @Test
    @DisplayName("예상 가능한 4xx 응답은 공통 응답 로그를 남기지 않는다")
    void expected_client_error_does_not_write_response_log() throws Exception {
        executeWithStatus(HttpServletResponse.SC_NOT_FOUND);

        assertEquals(0, appender.list.size());
    }

    @Test
    @DisplayName("운영 오류로 기록되지 않은 5xx 응답은 안전망 로그를 남긴다")
    void unlogged_server_error_writes_fallback_log() throws Exception {
        executeWithStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        assertEquals(1, appender.list.size());
        assertEquals("RES_ERROR", appender.list.get(0).getFormattedMessage());
    }

    @Test
    @DisplayName("이미 운영 오류로 기록된 5xx 응답은 중복 응답 로그를 남기지 않는다")
    void logged_server_error_does_not_write_duplicate_response_log() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setAttribute(AuditLogger.OPERATIONAL_ERROR_LOGGED_ATTRIBUTE, Boolean.TRUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        filter.doFilter(request, response, chain);

        assertEquals(0, appender.list.size());
    }

    private void executeWithStatus(int status) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(status);

        filter.doFilter(request, response, chain);
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.logging.LoggingFilterTest`

Expected: 기존 `LoggingFilter`가 200/404에서도 `RES_RESULT`를 남겨 테스트 실패한다.

- [ ] **Step 3: `LoggingFilter` 최소 수정**

`src/main/java/com/example/monghyang/domain/logging/LoggingFilter.java`의 `finally` 블록을 조건부 로그로 바꾼다.

```java
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString();
        MDC.put("requestURI", request.getRequestURI());
        MDC.put("method", request.getMethod());
        MDC.put("traceId", traceId);
        MDC.put("clientIp", request.getRemoteAddr());

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("status", String.valueOf(response.getStatus()));
            if (shouldWriteFallbackErrorLog(request, response)) {
                log.error("RES_ERROR");
            }
            MDC.clear();
        }
    }

    private boolean shouldWriteFallbackErrorLog(HttpServletRequest request, HttpServletResponse response) {
        return response.getStatus() >= 500
                && !Boolean.TRUE.equals(request.getAttribute(AuditLogger.OPERATIONAL_ERROR_LOGGED_ATTRIBUTE));
    }
}
```

- [ ] **Step 4: JSON 로그 provider 보강**

`src/main/resources/logback-spring.xml`의 `<providers>`에 `severity`와 `message`를 추가한다.

```xml
<providers>
    <timestamp timeZone="Asia/Seoul"/>
    <logLevel>
        <fieldName>severity</fieldName>
    </logLevel>
    <loggerName/>
    <threadName/>
    <message/>
    <mdc/>
    <arguments>
        <includeStructuredArguments>true</includeStructuredArguments>
        <includeNonStructuredArguments>false</includeNonStructuredArguments>
    </arguments>
    <stackTrace/>
</providers>
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.logging.LoggingFilterTest`

Expected: `BUILD SUCCESSFUL`

---

### Task 3: 양조장/판매자 쓰기 결과 감사 로그 추가

**Files:**
- Create: `src/main/java/com/example/monghyang/domain/logging/DomainWriteLoggingFilter.java`
- Test: `src/test/java/com/example/monghyang/domain/logging/DomainWriteLoggingFilterTest.java`

- [ ] **Step 1: 실패하는 필터 테스트 작성**

`src/test/java/com/example/monghyang/domain/logging/DomainWriteLoggingFilterTest.java`를 생성한다.

```java
package com.example.monghyang.domain.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainWriteLoggingFilterTest {
    @Mock
    AuditLogger auditLogger;

    @Test
    @DisplayName("양조장 전용 쓰기 요청은 성공 결과를 도메인 감사 로그로 남긴다")
    void brewery_write_success_writes_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/brewery-priv/joy-add");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);

        filter.doFilter(request, response, chain);

        verify(auditLogger).logDomainWriteResult(eq("BREWERY"), eq(request), eq(200), anyLong());
    }

    @Test
    @DisplayName("판매자 전용 쓰기 요청은 실패 결과도 도메인 감사 로그로 남긴다")
    void seller_write_failure_writes_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/seller-priv/product/10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_FORBIDDEN);

        filter.doFilter(request, response, chain);

        verify(auditLogger).logDomainWriteResult(eq("SELLER"), eq(request), eq(403), anyLong());
    }

    @Test
    @DisplayName("양조장 전용 조회 요청은 도메인 쓰기 감사 로그를 남기지 않는다")
    void brewery_get_request_does_not_write_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/brewery-priv/joy");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);

        filter.doFilter(request, response, chain);

        verify(auditLogger, never()).logDomainWriteResult(anyString(), any(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("일반 쓰기 요청은 도메인 쓰기 감사 로그를 남기지 않는다")
    void public_write_request_does_not_write_domain_audit_log() throws Exception {
        DomainWriteLoggingFilter filter = new DomainWriteLoggingFilter(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/community");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(HttpServletResponse.SC_OK);

        filter.doFilter(request, response, chain);

        verify(auditLogger, never()).logDomainWriteResult(anyString(), any(), anyInt(), anyLong());
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.logging.DomainWriteLoggingFilterTest`

Expected: `DomainWriteLoggingFilter` 클래스가 없어 컴파일 실패한다.

- [ ] **Step 3: 최소 구현 작성**

`src/main/java/com/example/monghyang/domain/logging/DomainWriteLoggingFilter.java`를 생성한다.

```java
package com.example.monghyang.domain.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 양조장 및 판매자 전용 쓰기 요청의 응답 결과를 감사 로그로 기록합니다.
 *
 * <p>조회 요청과 일반 사용자 쓰기 요청은 이 필터의 기록 대상이 아닙니다.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class DomainWriteLoggingFilter extends OncePerRequestFilter {
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String BREWERY_PRIV_PREFIX = "/api/brewery-priv";
    private static final String SELLER_PRIV_PREFIX = "/api/seller-priv";

    private final AuditLogger auditLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            String ownerType = ownerTypeOf(request);
            if (ownerType != null && WRITE_METHODS.contains(request.getMethod())) {
                long durationMs = System.currentTimeMillis() - startTime;
                auditLogger.logDomainWriteResult(ownerType, request, response.getStatus(), durationMs);
            }
        }
    }

    private String ownerTypeOf(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(BREWERY_PRIV_PREFIX)) {
            return "BREWERY";
        }
        if (requestURI.startsWith(SELLER_PRIV_PREFIX)) {
            return "SELLER";
        }
        return null;
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.logging.DomainWriteLoggingFilterTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 현재 쓰기 경로 범위 확인**

현재 계획의 필터는 다음 컨트롤러의 `POST`, `PUT`, `PATCH`, `DELETE` 요청을 모두 기록한다.

- `src/main/java/com/example/monghyang/domain/brewery/controller/BreweryPrivController.java`: 양조장 정보 수정/삭제/복구, 태그 수정, 체험 생성/수정/삭제/복구/품절 변경, 체험 일정 변경, 예약 변경/삭제, 별도 휴무일 지정/확정/해제, 양조장 운영시간/휴게시간 일정 변경
- `src/main/java/com/example/monghyang/domain/seller/controller/SellerPrivController.java`: 판매자 정보 수정/삭제/복구, 상품 생성/수정/삭제/복구, 재고 변경, 품절 변경, 상품 태그 수정

새 양조장/판매자 쓰기 API가 같은 prefix와 쓰기 HTTP method로 추가되면 별도 코드 수정 없이 기록된다.

---

### Task 4: 전역 예외 처리에서 운영 오류만 로그로 기록

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/global/advice/GlobalExceptionHandler.java`
- Test: `src/test/java/com/example/monghyang/domain/global/advice/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: 실패하는 예외 처리 테스트 작성**

`src/test/java/com/example/monghyang/domain/global/advice/GlobalExceptionHandlerTest.java`를 생성한다.

```java
package com.example.monghyang.domain.global.advice;

import com.example.monghyang.domain.logging.AuditLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.TransactionSystemException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @Mock
    AuditLogger auditLogger;

    @Test
    @DisplayName("예상 가능한 비즈니스 4xx 예외는 로그를 남기지 않는다")
    void application_exception_for_expected_business_error_does_not_write_log() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/joy/1");

        handler.applicationException(request, new ApplicationException(ApplicationError.JOY_NOT_FOUND));

        verify(auditLogger, never()).logOperationalError(any(), any(), any(), anyInt(), any());
        verify(auditLogger, never()).logSecurityFailure(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("5xx 애플리케이션 예외는 운영 오류 로그를 남긴다")
    void application_exception_for_server_error_writes_operational_log() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/images/1");
        ApplicationException exception = new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR, new RuntimeException("s3 fail"));

        handler.applicationException(request, exception);

        verify(auditLogger).logOperationalError(
                eq("APPLICATION_EXCEPTION"),
                eq(request),
                eq("IMAGE_LOAD_ERROR"),
                eq(500),
                eq(exception)
        );
    }

    @Test
    @DisplayName("트랜잭션 예외는 응답 상태와 관계없이 운영 오류 로그를 남긴다")
    void transaction_exception_writes_operational_log() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
        TransactionSystemException exception = new TransactionSystemException("transaction failed");

        handler.transactionSystemException(request, exception);

        verify(auditLogger).logOperationalError(
                eq("TRANSACTION_EXCEPTION"),
                eq(request),
                eq("TransactionSystemException"),
                eq(500),
                eq(exception)
        );
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.global.advice.GlobalExceptionHandlerTest`

Expected: `GlobalExceptionHandler` 생성자가 맞지 않거나 `AuditLogger` 호출이 없어 실패한다.

- [ ] **Step 3: `GlobalExceptionHandler` 수정**

`org.jboss.logging.MDC` 의존을 제거하고 `AuditLogger`를 생성자 주입한다.

```java
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final AuditLogger auditLogger;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApplicationErrorDto> applicationException(HttpServletRequest request, ApplicationException e) {
        if (isOperationalApplicationError(e)) {
            auditLogger.logOperationalError(
                    "APPLICATION_EXCEPTION",
                    request,
                    e.getApplicationError().name(),
                    e.getHttpStatus().value(),
                    e
            );
        } else if (isUnloggedSecurityError(request, e)) {
            auditLogger.logSecurityFailure(
                    "SECURITY_EXCEPTION",
                    request,
                    null,
                    null,
                    e.getApplicationError()
            );
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApplicationErrorDto.requestStatusMessageOf(request, e.getHttpStatus(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApplicationErrorDto> methodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        List<ObjectError> objectErrors = e.getBindingResult().getAllErrors();
        List<String> errors = new ArrayList<>();
        for (ObjectError objectError : objectErrors) {
            errors.add(objectError.getDefaultMessage());
        }
        String error = String.join(" ", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApplicationErrorDto> methodArgumentTypeMismatchException(HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        String message = "요청 파라미터 '" + e.getName() + "'를 올바른 타입으로 넘겨주세요.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApplicationErrorDto> transactionSystemException(HttpServletRequest request, TransactionSystemException e) {
        Throwable root = e.getMostSpecificCause();
        if (root instanceof ConstraintViolationException cve) {
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<?> constraintViolation : cve.getConstraintViolations()) {
                errors.add(constraintViolation.getMessage());
            }
            String error = String.join(" ", errors);
            auditLogger.logOperationalError("TRANSACTION_EXCEPTION", request, "TransactionSystemException", HttpStatus.BAD_REQUEST.value(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, error));
        }

        auditLogger.logOperationalError("TRANSACTION_EXCEPTION", request, "TransactionSystemException", HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 처리 중 에러가 발생했습니다. 서버 관리자에게 문의하세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApplicationErrorDto> dataIntegrityViolationException(HttpServletRequest request, DataIntegrityViolationException e) {
        auditLogger.logOperationalError("DATA_INTEGRITY_VIOLATION", request, "DataIntegrityViolationException", HttpStatus.BAD_REQUEST.value(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, "DB의 데이터 무결성 제약조건 검증을 통과하지 못한 값입니다. 다른 값을 입력해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorDto> exception(HttpServletRequest request, Exception e) {
        auditLogger.logOperationalError("UNHANDLED_EXCEPTION", request, e.getClass().getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    private boolean isOperationalApplicationError(ApplicationException e) {
        return e.getHttpStatus().is5xxServerError()
                || e.getErrorType() == ErrorType.SYSTEM
                || e.getErrorType() == ErrorType.DB;
    }

    private boolean isUnloggedSecurityError(HttpServletRequest request, ApplicationException e) {
        return e.getErrorType() == ErrorType.SECURITY
                && !Boolean.TRUE.equals(request.getAttribute(AuditLogger.SECURITY_EVENT_LOGGED_ATTRIBUTE));
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.global.advice.GlobalExceptionHandlerTest`

Expected: `BUILD SUCCESSFUL`

---

### Task 5: 인증/인가 핸들러 감사 로그 추가

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/security/authHandler/SessionLoginSuccessHandler.java`
- Modify: `src/main/java/com/example/monghyang/domain/security/authHandler/SessionLoginFailureHandler.java`
- Modify: `src/main/java/com/example/monghyang/domain/security/authHandler/CustomAuthenticationEntryPoint.java`
- Modify: `src/main/java/com/example/monghyang/domain/security/authHandler/CustomAccessDeniedHandler.java`
- Modify: `src/main/java/com/example/monghyang/domain/security/authHandler/CustomLogoutHandler.java`
- Modify: `src/main/java/com/example/monghyang/domain/security/authHandler/SessionLogoutSuccessHandler.java`
- Modify: `src/main/java/com/example/monghyang/domain/util/SecurityFilterExceptionResponseWriter.java`
- Test: `src/test/java/com/example/monghyang/domain/security/authHandler/SessionLoginSuccessHandlerTest.java`
- Test: `src/test/java/com/example/monghyang/domain/security/authHandler/SessionLoginFailureHandlerTest.java`

- [ ] **Step 1: 로그인 성공/실패 테스트 작성**

`src/test/java/com/example/monghyang/domain/security/authHandler/SessionLoginSuccessHandlerTest.java`를 생성한다.

```java
package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.auth.details.LoginUserDetails;
import com.example.monghyang.domain.logging.AuditLogger;
import com.example.monghyang.domain.util.SessionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionLoginSuccessHandlerTest {
    @Mock
    SessionUtil sessionUtil;
    @Mock
    AuditLogger auditLogger;

    @Test
    @DisplayName("폼 로그인 성공은 세션 생성 후 성공 감사 로그를 남긴다")
    void login_success_writes_audit_log_after_session_created() throws Exception {
        SessionLoginSuccessHandler handler = new SessionLoginSuccessHandler(new ObjectMapper(), sessionUtil, auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        LoginUserDetails principal = mock(LoginUserDetails.class);
        when(principal.getUsername()).thenReturn("사용자");
        when(principal.getUserId()).thenReturn(1L);
        when(principal.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(sessionUtil).createNewAuthInfo(request, response, 1L, "ROLE_USER");
        verify(auditLogger).logSecuritySuccess("LOGIN_SUCCESS", request, 1L, "ROLE_USER");
    }
}
```

`src/test/java/com/example/monghyang/domain/security/authHandler/SessionLoginFailureHandlerTest.java`를 생성한다.

```java
package com.example.monghyang.domain.security.authHandler;

import com.example.monghyang.domain.global.advice.ApplicationError;
import com.example.monghyang.domain.logging.AuditLogger;
import com.example.monghyang.domain.util.SecurityFilterExceptionResponseWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionLoginFailureHandlerTest {
    @Mock
    SecurityFilterExceptionResponseWriter writer;
    @Mock
    AuditLogger auditLogger;

    @Test
    @DisplayName("폼 로그인 실패는 실패 감사 로그를 남기고 오류 응답을 작성한다")
    void login_failure_writes_audit_log() throws Exception {
        SessionLoginFailureHandler handler = new SessionLoginFailureHandler(writer, auditLogger);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));

        verify(auditLogger).logSecurityFailure("LOGIN_FAILURE", request, null, null, ApplicationError.USER_UNAUTHORIZED);
        verify(writer).setApplicationErrorResponse(eq(request), eq(response), any());
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.security.authHandler.SessionLoginSuccessHandlerTest --tests com.example.monghyang.domain.security.authHandler.SessionLoginFailureHandlerTest`

Expected: 생성자 시그니처와 `AuditLogger` 호출이 없어 실패한다.

- [ ] **Step 3: 보안 핸들러 구현 수정**

`SessionLoginSuccessHandler`는 `AuditLogger`를 주입하고 세션/토큰 생성 이후에 성공 로그를 남긴다.

```java
@Component
@RequiredArgsConstructor
public class SessionLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final SessionUtil sessionUtil;
    private final AuditLogger auditLogger;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        LoginUserDetails loginUserDetails = (LoginUserDetails) authentication.getPrincipal();
        String nickname = loginUserDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = loginUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        String role = iterator.next().getAuthority();
        Long userId = loginUserDetails.getUserId();

        sessionUtil.createNewAuthInfo(request, response, userId, role);
        auditLogger.logSecuritySuccess("LOGIN_SUCCESS", request, userId, role);

        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), LoginDto.nicknameRoleOf(nickname, role));
    }
}
```

`SessionLoginFailureHandler`는 실패 감사 로그를 남긴 뒤 기존 오류 응답을 유지한다.

```java
@Component
@RequiredArgsConstructor
public class SessionLoginFailureHandler implements AuthenticationFailureHandler {
    private final SecurityFilterExceptionResponseWriter writer;
    private final AuditLogger auditLogger;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        auditLogger.logSecurityFailure("LOGIN_FAILURE", request, null, null, ApplicationError.USER_UNAUTHORIZED);
        writer.setApplicationErrorResponse(request, response, new ApplicationException(ApplicationError.USER_UNAUTHORIZED));
    }
}
```

`CustomAuthenticationEntryPoint`는 인증 필요 이벤트를 기록한다.

```java
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final SecurityFilterExceptionResponseWriter writer;
    private final AuditLogger auditLogger;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        auditLogger.logSecurityFailure("AUTHENTICATION_REQUIRED", request, null, null, ApplicationError.SESSION_NOT_FOUND);
        writer.setApplicationErrorResponse(request, response, new ApplicationException(ApplicationError.SESSION_NOT_FOUND));
    }
}
```

`CustomAccessDeniedHandler`는 권한 거부 이벤트를 기록한다.

```java
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityFilterExceptionResponseWriter writer;
    private final AuditLogger auditLogger;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        auditLogger.logSecurityFailure("AUTHORIZATION_DENIED", request, null, null, ApplicationError.REQUEST_FORBIDDEN);
        writer.setApplicationErrorResponse(request, response, new ApplicationException(ApplicationError.REQUEST_FORBIDDEN));
    }
}
```

`SecurityFilterExceptionResponseWriter`는 직접 MDC를 쓰지 않고 응답 작성만 담당하도록 줄인다.

```java
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilterExceptionResponseWriter {
    private final ObjectMapper objectMapper;

    /**
     * 보안 필터 단계에서 발생한 애플리케이션 오류를 JSON 응답으로 작성합니다.
     *
     * @param request 요청 객체
     * @param response 응답 객체
     * @param e 응답으로 변환할 애플리케이션 예외
     */
    public void setApplicationErrorResponse(HttpServletRequest request, HttpServletResponse response, ApplicationException e) {
        response.setStatus(e.getHttpStatus().value());
        response.setContentType("application/json;charset=utf-8");
        try {
            objectMapper.writeValue(response.getWriter(), ApplicationErrorDto.requestStatusMessageOf(request, e.getHttpStatus(), e.getMessage()));
        } catch (IOException ex) {
            log.error("SECURITY_ERROR_RESPONSE_WRITE_FAILED", ex);
        }
    }
}
```

- [ ] **Step 4: 로그아웃 감사 로그 추가**

`CustomLogoutHandler`에서 refresh token 원문을 기록하지 않고, 파싱된 `userId`만 성공 핸들러가 사용할 수 있도록 request attribute에 넣는다. 기존 응답 흐름은 바꾸지 않는다.

```java
public static final String LOGOUT_USER_ID_ATTRIBUTE = CustomLogoutHandler.class.getName() + ".logoutUserId";
public static final String LOGOUT_FAILURE_ATTRIBUTE = CustomLogoutHandler.class.getName() + ".logoutFailure";

@Override
public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String refreshToken = request.getHeader("X-Refresh-Token");
    if (refreshToken == null) {
        auditLogger.logSecurityFailure("LOGOUT_FAILURE", request, null, null, ApplicationError.REFRESH_TOKEN_NOT_FOUND);
        throw new ApplicationException(ApplicationError.REFRESH_TOKEN_NOT_FOUND);
    }

    String tid = "";
    Long userId = null;

    try {
        JwtClaimsDto jwtClaimsDto = jwtUtil.parseRefreshToken(refreshToken);
        tid = jwtClaimsDto.getTid();
        userId = jwtClaimsDto.getUserId();
        request.setAttribute(LOGOUT_USER_ID_ATTRIBUTE, userId);

        if (userId != null) {
            redisService.deleteRefreshTokenAndSession(userId, tid);
        }
    } catch (ApplicationException e) {
        request.setAttribute(LOGOUT_FAILURE_ATTRIBUTE, Boolean.TRUE);
        auditLogger.logSecurityFailure("LOGOUT_FAILURE", request, userId, null, e.getApplicationError());
    } catch (Exception e) {
        request.setAttribute(LOGOUT_FAILURE_ATTRIBUTE, Boolean.TRUE);
        auditLogger.logOperationalError("LOGOUT_OPERATIONAL_ERROR", request, e.getClass().getSimpleName(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
    }
}
```

`SessionLogoutSuccessHandler`에서 성공 로그를 남긴다.

```java
@Component
@RequiredArgsConstructor
public class SessionLogoutSuccessHandler implements LogoutSuccessHandler {
    private final ObjectMapper objectMapper;
    private final AuditLogger auditLogger;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Long userId = (Long) request.getAttribute(CustomLogoutHandler.LOGOUT_USER_ID_ATTRIBUTE);
        if (!Boolean.TRUE.equals(request.getAttribute(CustomLogoutHandler.LOGOUT_FAILURE_ATTRIBUTE))) {
            auditLogger.logSecuritySuccess("LOGOUT_SUCCESS", request, userId, null);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");
        objectMapper.writeValue(response.getWriter(), LogoutDto.successContentFrom());
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.security.authHandler.SessionLoginSuccessHandlerTest --tests com.example.monghyang.domain.security.authHandler.SessionLoginFailureHandlerTest`

Expected: `BUILD SUCCESSFUL`

---

### Task 6: OAuth2, 토큰 갱신, 계정 중요 이벤트 감사 로그 추가

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/oauth2/handler/CustomOAuth2AuthenticationSuccessHandler.java`
- Modify: `src/main/java/com/example/monghyang/domain/oauth2/handler/CustomOAuth2AuthenticationFailureHandler.java`
- Modify: `src/main/java/com/example/monghyang/domain/auth/service/AuthService.java`
- Modify: `src/main/java/com/example/monghyang/domain/users/controller/UsersController.java`
- Test: `src/test/java/com/example/monghyang/domain/auth/service/AuthServiceAuditLogTest.java`

- [ ] **Step 1: 토큰 갱신 감사 로그 테스트 작성**

`src/test/java/com/example/monghyang/domain/auth/service/AuthServiceAuditLogTest.java`를 생성한다. 기존 `AuthService` 생성자 의존성이 많으므로 모든 의존성은 Mockito mock으로 주입하고, 토큰 갱신 경로에 필요한 mock만 동작시킨다.

```java
package com.example.monghyang.domain.auth.service;

import com.example.monghyang.domain.brewery.repository.BreweryImageRepository;
import com.example.monghyang.domain.brewery.repository.BreweryRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyBreakTimeRepository;
import com.example.monghyang.domain.brewery.repository.BreweryWeeklyOpenTimeRepository;
import com.example.monghyang.domain.brewery.repository.RegionTypeRepository;
import com.example.monghyang.domain.image.service.StorageService;
import com.example.monghyang.domain.logging.AuditLogger;
import com.example.monghyang.domain.redis.RedisService;
import com.example.monghyang.domain.seller.repository.SellerImageRepository;
import com.example.monghyang.domain.seller.repository.SellerRepository;
import com.example.monghyang.domain.users.repository.RoleRepository;
import com.example.monghyang.domain.users.repository.UsersRepository;
import com.example.monghyang.domain.util.JwtUtil;
import com.example.monghyang.domain.util.SessionUtil;
import com.example.monghyang.domain.util.dto.JwtClaimsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceAuditLogTest {
    @Mock UsersRepository usersRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock RoleRepository roleRepository;
    @Mock JwtUtil jwtUtil;
    @Mock RedisService redisService;
    @Mock SessionUtil sessionUtil;
    @Mock SellerRepository sellerRepository;
    @Mock BreweryRepository breweryRepository;
    @Mock RegionTypeRepository regionTypeRepository;
    @Mock StorageService storageService;
    @Mock BreweryImageRepository breweryImageRepository;
    @Mock SellerImageRepository sellerImageRepository;
    @Mock BreweryWeeklyOpenTimeRepository breweryWeeklyOpenTimeRepository;
    @Mock BreweryWeeklyBreakTimeRepository breweryWeeklyBreakTimeRepository;
    @Mock AuditLogger auditLogger;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                usersRepository,
                passwordEncoder,
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
                Clock.systemDefaultZone(),
                auditLogger
        );
    }

    @Test
    @DisplayName("토큰 갱신 성공은 성공 감사 로그를 남긴다")
    void token_refresh_success_writes_audit_log() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Refresh-Token", "masked");
        JwtClaimsDto claims = JwtClaimsDto.tidUserIdDeviceTypeRoleOf("tid", 1L, "ROLE_USER");
        given(jwtUtil.parseRefreshToken("masked")).willReturn(claims);

        authService.updateRefreshToken(request, response);

        verify(redisService).deleteRefreshTokenAndSession(1L, "tid");
        verify(sessionUtil).createNewAuthInfo(request, response, 1L, "ROLE_USER");
        verify(auditLogger).logSecuritySuccess("TOKEN_REFRESH_SUCCESS", request, 1L, "ROLE_USER");
    }
}
```

- [ ] **Step 2: 실패 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.auth.service.AuthServiceAuditLogTest`

Expected: `AuthService` 생성자 시그니처와 감사 로그 호출이 맞지 않아 실패한다.

- [ ] **Step 3: `AuthService`에 감사 로그 추가**

`AuditLogger`를 생성자 주입 필드로 추가한다. 토큰 갱신은 성공 후 로그를 남기고, 갱신 가능한 토큰이 없는 실패는 실패 감사 로그를 남긴 뒤 기존 예외를 유지한다.

```java
private final AuditLogger auditLogger;

@Transactional
public void updateRefreshToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = request.getHeader("X-Refresh-Token");
    if (refreshToken == null || refreshToken.isEmpty()) {
        auditLogger.logSecurityFailure("TOKEN_REFRESH_FAILURE", request, null, null, ApplicationError.TOKEN_EXPIRED);
        throw new ApplicationException(ApplicationError.TOKEN_EXPIRED);
    }

    try {
        JwtClaimsDto jwtClaimsDto = jwtUtil.parseRefreshToken(refreshToken);
        Long userId = jwtClaimsDto.getUserId();
        String tid = jwtClaimsDto.getTid();
        String role = jwtClaimsDto.getRole();

        redisService.deleteRefreshTokenAndSession(userId, tid);
        sessionUtil.createNewAuthInfo(request, response, userId, role);
        auditLogger.logSecuritySuccess("TOKEN_REFRESH_SUCCESS", request, userId, role);
    } catch (ApplicationException e) {
        auditLogger.logSecurityFailure("TOKEN_REFRESH_FAILURE", request, null, null, e.getApplicationError());
        throw e;
    }
}
```

비밀번호 초기화와 검증은 성공/실패 모두 중요 계정 이벤트로 기록한다. 비밀번호 원문이나 이메일 원문은 남기지 않는다.

```java
public void resetPassword(ReqResetPwDto dto, HttpServletRequest request) {
    String password = bCryptPasswordEncoder.encode(dto.getNewPassword());
    Users users = usersRepository.findByEmail(dto.getEmail()).orElseThrow(() ->
            new ApplicationException(ApplicationError.USER_NOT_FOUND));
    users.updatePassword(password);
    usersRepository.save(users);
    auditLogger.logSecuritySuccess("PASSWORD_RESET_SUCCESS", request, users.getId(), users.getRole().getName().name());
}

public void checkPassword(Long userId, VerifyAuthDto verifyAuthDto, HttpServletRequest request) {
    Users users = usersRepository.findById(userId).orElseThrow(() ->
            new ApplicationException(ApplicationError.USER_NOT_FOUND));
    if (!bCryptPasswordEncoder.matches(verifyAuthDto.getPassword(), users.getPassword())) {
        auditLogger.logSecurityFailure("PASSWORD_VERIFY_FAILURE", request, userId, null, ApplicationError.NOT_MATCH_CUR_PASSWORD);
        throw new ApplicationException(ApplicationError.NOT_MATCH_CUR_PASSWORD);
    }
    auditLogger.logSecuritySuccess("PASSWORD_VERIFY_SUCCESS", request, userId, null);
}
```

위 시그니처 변경에 맞춰 `AuthController.resetPw`와 `AuthController.checkPassword`는 `HttpServletRequest`를 인자로 추가해 전달한다.

기존 `src/test/java/com/example/monghyang/devtest/AuthServiceTest.java`도 생성자와 메서드 호출을 맞춘다.

```java
@Mock
AuditLogger auditLogger;
```

```java
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
        FIXED_CLOCK,
        auditLogger
);
```

```java
MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/reset-pw");
authService.resetPassword(dto, request);
```

```java
MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/verify-pw");
authService.checkPassword(userId, dto, request);
```

- [ ] **Step 4: OAuth2 핸들러 감사 로그 추가**

`CustomOAuth2AuthenticationSuccessHandler`에 `AuditLogger`를 주입한다.

```java
if (user.getPhone() == null) {
    request.getSession().setAttribute("incompleteUserId", user.getId());
    auditLogger.logSecuritySuccess("OAUTH2_LOGIN_INCOMPLETE", request, user.getId(), null);
    response.sendRedirect(clientUrl + "/auth/complete");
} else {
    Long userId = user.getId();
    Collection<? extends GrantedAuthority> authorities = customOAuth2UserDetails.getAuthorities();
    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
    GrantedAuthority grantedAuthority = iterator.next();
    String role = grantedAuthority.getAuthority();
    sessionUtil.createNewAuthInfo(request, response, userId, role);
    auditLogger.logSecuritySuccess("OAUTH2_LOGIN_SUCCESS", request, userId, role);
}
```

`CustomOAuth2AuthenticationFailureHandler`는 기존 `logger.error` 대신 감사 로그를 사용한다.

```java
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final AuditLogger auditLogger;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        auditLogger.logSecurityFailure("OAUTH2_LOGIN_FAILURE", request, null, null, ApplicationError.USER_UNAUTHORIZED);
        String errorMsg = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        response.setContentType("/login?error=" + errorMsg);
    }
}
```

- [ ] **Step 5: 회원 수정/탈퇴 감사 로그 추가**

`UsersController`에 `AuditLogger`를 주입하고 성공 응답 직전에 로그를 남긴다.

```java
auditLogger.logSecuritySuccess("USER_UPDATE_SUCCESS", request, userId, userRole);
return ResponseEntity.ok().body(ResponseDataDto.success("회원 수정이 완료되었습니다. 다시 로그인 해주세요."));
```

```java
auditLogger.logSecuritySuccess("USER_WITHDRAWAL_SUCCESS", request, userId, userRole);
return ResponseEntity.ok().body(ResponseDataDto.success("회원 탈퇴가 완료되었습니다."));
```

- [ ] **Step 6: 테스트 통과 확인**

Run: `./gradlew test --tests com.example.monghyang.domain.auth.service.AuthServiceAuditLogTest`

Expected: `BUILD SUCCESSFUL`

---

### Task 7: 외부 연동 실패 중복 로그 제거 및 cause 보존

**Files:**
- Modify: `src/main/java/com/example/monghyang/domain/util/JwtUtil.java`
- Modify: `src/main/java/com/example/monghyang/domain/image/service/impl/AwsStorageService.java`
- Modify: `src/main/java/com/example/monghyang/domain/redis/RedisService.java`

- [ ] **Step 1: `JwtUtil`의 토큰 훼손 로컬 로그 제거**

토큰 원문은 이미 기록하지 않지만, 실패 감사 로그는 요청 컨텍스트를 가진 호출부에서 남기는 것이 더 정확하다. `JwtUtil`은 cause만 보존한다.

```java
} catch (JwtException | IllegalArgumentException e) {
    throw new ApplicationException(ApplicationError.TOKEN_IMPAIRED, e);
}
```

- [ ] **Step 2: `AwsStorageService`의 로컬 중복 로그 제거**

전역 예외 처리기가 `ApplicationException`의 cause를 운영 오류로 기록할 수 있도록 cause를 보존한다.

```java
} catch (IOException e) {
    throw new ApplicationException(ApplicationError.IMAGE_UPLOAD_ERROR, e);
} catch (S3Exception e) {
    throw new ApplicationException(ApplicationError.IMAGE_UPLOAD_ERROR, e);
}
```

```java
} catch (S3Exception e) {
    if (e.statusCode() == 404) {
        throw new ApplicationException(ApplicationError.IMAGE_NOT_FOUND, e);
    }
    throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR, e);
} catch (MalformedURLException e) {
    throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR, e);
} catch (Exception e) {
    throw new ApplicationException(ApplicationError.IMAGE_LOAD_ERROR, e);
}
```

```java
} catch (S3Exception e) {
    if (e.statusCode() == 404) {
        throw new ApplicationException(ApplicationError.IMAGE_NOT_FOUND, e);
    }
    throw new ApplicationException(ApplicationError.IMAGE_REMOVE_ERROR, e);
}
```

- [ ] **Step 3: `RedisService` 로그에서 세션 ID 제거**

request 컨텍스트가 없는 내부 정리 작업은 기존 로거를 유지하되 세션 ID 원문은 기록하지 않는다.

```java
} catch (Exception e) {
    log.warn("세션 삭제 중 예외 발생", e);
}
```

`deleteAllInfoByUserId`와 `deleteRefreshTokenByUserIdAndSid`는 userId만 유지한다.

- [ ] **Step 4: 컴파일 확인**

Run: `./gradlew compileJava`

Expected: `BUILD SUCCESSFUL`

---

### Task 8: 통합 검증 및 회귀 점검

**Files:**
- Verify only

- [ ] **Step 1: 변경된 단위 테스트 실행**

Run:

```bash
./gradlew test --tests com.example.monghyang.domain.logging.AuditLoggerTest --tests com.example.monghyang.domain.logging.LoggingFilterTest --tests com.example.monghyang.domain.logging.DomainWriteLoggingFilterTest --tests com.example.monghyang.domain.global.advice.GlobalExceptionHandlerTest --tests com.example.monghyang.domain.security.authHandler.SessionLoginSuccessHandlerTest --tests com.example.monghyang.domain.security.authHandler.SessionLoginFailureHandlerTest --tests com.example.monghyang.domain.auth.service.AuthServiceAuditLogTest
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: 전체 테스트 실행**

Run: `./gradlew test`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 빌드 실행**

Run: `./gradlew build`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 로그 정책 수동 점검**

로컬 실행 환경이 준비되어 있으면 다음을 수동으로 확인한다.

- 일반 조회 200: `RES_RESULT`, `SECURITY_AUDIT`, `OPERATIONAL_ERROR` 없음
- 일반 조회 결과 없음 404 또는 빈 결과 200: 공통 응답 로그 없음
- 로그인 성공: `SECURITY_AUDIT`, `eventName=LOGIN_SUCCESS`, `eventOutcome=SUCCESS`
- 로그인 실패: `SECURITY_AUDIT`, `eventName=LOGIN_FAILURE`, `eventOutcome=FAILURE`
- 인증 없는 보호 API 접근: `SECURITY_AUDIT`, `eventName=AUTHENTICATION_REQUIRED`
- 권한 부족: `SECURITY_AUDIT`, `eventName=AUTHORIZATION_DENIED`
- 양조장 쓰기 성공: `DOMAIN_WRITE_RESULT`, `eventName=BREWERY_WRITE_RESULT`, `eventOutcome=SUCCESS`
- 양조장 쓰기 4xx 실패: `DOMAIN_WRITE_RESULT`, `eventName=BREWERY_WRITE_RESULT`, `eventOutcome=FAILURE`
- 판매자 쓰기 성공: `DOMAIN_WRITE_RESULT`, `eventName=SELLER_WRITE_RESULT`, `eventOutcome=SUCCESS`
- 판매자 쓰기 4xx 실패: `DOMAIN_WRITE_RESULT`, `eventName=SELLER_WRITE_RESULT`, `eventOutcome=FAILURE`
- 양조장/판매자 조회 GET: `DOMAIN_WRITE_RESULT` 없음
- 강제 5xx: `OPERATIONAL_ERROR` 또는 누락 시 `RES_ERROR`

- [ ] **Step 5: 자체 검토**

다음 질문에 답하고 불필요한 구현이 확인되면 구현을 줄인다.

- 새 파일 수가 요구 대비 과하지 않은가? `AuditLogger`, `DomainWriteLoggingFilter` 외의 새 추상화가 생겼다면 제거한다.
- 일반 정상/예상 4xx 경로가 다시 `INFO` 로그를 남기지 않는가?
- 양조장/판매자 쓰기 작업의 성공/실패 결과가 누락 없이 기록되는가?
- 인증/인가 성공/실패 이벤트가 누락되지 않았는가?
- 운영 오류가 최소 한 번은 기록되고, 같은 요청에서 중복으로 과도하게 찍히지 않는가?
- 토큰, 비밀번호, 쿠키, 세션 ID 원문이 로그 필드나 메시지에 포함되지 않는가?
- 테스트 편의를 위해 production 코드에 setter, test-only constructor, visibility 완화를 추가하지 않았는가?

## 완료 기준

- 일반 API 정상/예상 경로에서 전량 응답 로그가 사라진다.
- 인증/인가 및 중요 계정 이벤트는 성공/실패 모두 구조화 감사 로그로 남는다.
- 양조장/판매자 전용 쓰기 API의 성공/실패 결과는 모두 `DOMAIN_WRITE_RESULT` 구조화 감사 로그로 남는다.
- 양조장/판매자 전용 조회 API와 일반 사용자 API는 도메인 쓰기 감사 로그를 남기지 않는다.
- 5xx, 미처리 예외, 트랜잭션/DB 정합성 문제, AWS S3 같은 외부 연동 실패는 운영 오류 로그로 남는다.
- 로그 JSON에 `severity`, `message`, MDC 필드, stack trace가 포함된다.
- 새 테스트와 전체 테스트가 통과한다.
- 코드베이스 변경 커밋은 사용자 확인 전까지 수행하지 않는다.
