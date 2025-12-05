package com.example.monghyang.domain.global.advice;

/**
 * 에러 발생 위치 종류
 */
public enum ErrorType {
    BUSINESS, // 애플리케이션 내부에서 발생한 비즈니스 로직 예외 상황(ApplicationException)
    SECURITY, // security filter 및 보안 관련 예외
    DB, // DB에서 발생한 예외
    SYSTEM // 시스템에서 발생한 예외(ex: IOException)
}
