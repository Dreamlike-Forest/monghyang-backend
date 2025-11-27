package com.example.monghyang.domain.global.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class) // 커스텀 예외 처리
    public ResponseEntity<ApplicationErrorDto> applicationException(HttpServletRequest request, ApplicationException e) {
        MDC.put("level", e.getLogLevel().toString());
        MDC.put("message", e.getMessage());
        if(e.getLogStackTrace() == true) {
            log.warn("stackTrace", e);
        }
        return ResponseEntity.status(e.getHttpStatus()).body(ApplicationErrorDto.requestStatusMessageOf(request, e.getHttpStatus(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // 유효하지 않은 값이 입력된 요청을 받았을 때 발생하는 예외 처리
    public ResponseEntity<ApplicationErrorDto> methodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        // 하나의 요청에 여러 개의 '유효하지 않은 값 에러'가 동시에 발생할 수 있다.
        List<ObjectError> objectErrors = e.getBindingResult().getAllErrors();
        List<String> errors = new ArrayList<>();
        for (ObjectError objectError : objectErrors) {
            errors.add(objectError.getDefaultMessage());
        }
        String error = String.join(" ", errors); // 클라이언트에 반환하기 위해 처리된 최종 에러 메시지 문자열
        MDC.put("level", LogLevel.INFO.toString());
        MDC.put("message", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApplicationErrorDto> methodArgumentTypeMismatchException(HttpServletRequest request, MethodArgumentTypeMismatchException e) {
        String message = "요청 파라미터 '"+e.getName()+"'를 올바른 타입으로 넘겨주세요.";
        MDC.put("level", LogLevel.INFO.toString());
        MDC.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApplicationErrorDto> transactionSystemException(HttpServletRequest request, TransactionSystemException e) {
        MDC.put("level", LogLevel.ERROR.toString());
        Throwable root = e.getMostSpecificCause();
        if (root instanceof ConstraintViolationException cve) {
            // 애플리케이션 레벨 Entity 필드 유효성 검증 예외 처리
            List<String> errors = new ArrayList<>();
            for (ConstraintViolation<?> constraintViolation : cve.getConstraintViolations()) {
                errors.add(constraintViolation.getMessage());
            }
            String error = String.join(" ", errors);
//            log.error("{} : {}", cve.getCause(), cve.getMessage());
            MDC.put("message", error);
            log.error("stackTrace", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, error));
        }

        String error = "트랜잭션 처리 에러 발생"+e.getCause()+" : "+e.getMessage();
        MDC.put("message", error);
        log.error("stackTrace", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 처리 중 에러가 발생했습니다. 서버 관리자에게 문의하세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // DB레벨 무결성 제약조건 위배 예외 처리(not null, uk, fk 제약조건 위배, 데이터 길이 초과 등)
    public ResponseEntity<ApplicationErrorDto> dataIntegrityViolationException(HttpServletRequest request, DataIntegrityViolationException e) {
        MDC.put("level", LogLevel.WARN.toString());
        MDC.put("message", e.getMessage());
        log.warn("stackTrace", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.BAD_REQUEST, "DB의 데이터 무결성 제약조건 검증을 통과하지 못한 값입니다. 다른 값을 입력해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorDto> exception(HttpServletRequest request, Exception e){
        MDC.put("level", LogLevel.ERROR.toString());
        MDC.put("message", e.getMessage());
        log.error("stackTrace", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApplicationErrorDto.requestStatusMessageOf(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

}
