package com.example.monghyang.domain.global.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApplicationErrorDto> applicationException(ApplicationException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(ApplicationErrorDto.statusMessageOf(e.getHttpStatus(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // 유효하지 않은 값이 입력된 요청을 받았을 때 발생하는 예외 처리
    public ResponseEntity<ApplicationErrorDto> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 하나의 요청에 여러 개의 '유효하지 않은 값 에러'가 동시에 발생할 수 있다.
        List<ObjectError> objectErrors = e.getBindingResult().getAllErrors();
        List<String> errors = new ArrayList<>();
        for (ObjectError objectError : objectErrors) {
            errors.add(objectError.getDefaultMessage());
        }
        String error = String.join(" ", errors); // 클라이언트에 반환하기 위해 처리된 최종 에러 메시지 문자열
        log.error("{} : {}", e.getCause(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApplicationErrorDto.statusMessageOf(HttpStatus.BAD_REQUEST, error));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApplicationErrorDto> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error(e.getMessage());
        String paramName = e.getName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApplicationErrorDto.statusMessageOf(HttpStatus.BAD_REQUEST, "요청 파라미터 '"+paramName+"'를 올바른 타입으로 넘겨주세요."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // DB 무결성 제약조건 위배 예외 처리(not null, uk, fk 제약조건 위배, 데이터 길이 초과 등)
    public ResponseEntity<ApplicationErrorDto> dataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApplicationErrorDto.statusMessageOf(HttpStatus.BAD_REQUEST, "DB의 데이터 무결성 제약조건을 위배하는 값입니다. 다른 값을 입력해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApplicationErrorDto> exception(Exception e){
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApplicationErrorDto.statusMessageOf(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

}
