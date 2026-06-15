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
import static org.mockito.ArgumentMatchers.anyString;
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

        verify(auditLogger, never()).logOperationalError(anyString(), any(), anyString(), anyInt(), any());
        verify(auditLogger, never()).logSecurityFailure(anyString(), any(), any(), any(), any());
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
    @DisplayName("트랜잭션 예외는 운영 오류 로그를 남긴다")
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
