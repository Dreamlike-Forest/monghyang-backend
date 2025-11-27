package com.example.monghyang.domain.global.advice;

/**
 * 중요도 정도
 */
public enum LogLevel {
    INFO, // 정상적인 동작을 기록
    WARN, // 주의: 주요 비즈니스 로직 수행과 관련
    ERROR // 심각: 반드시 로그를 별도의 파일로 기록
}
