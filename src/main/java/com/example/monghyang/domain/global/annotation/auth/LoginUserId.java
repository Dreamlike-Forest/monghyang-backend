package com.example.monghyang.domain.global.annotation.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 적용 범위: 파라메터
@Retention(RetentionPolicy.RUNTIME) // runtime에도 jvm이 읽을 수 있도록 설정
public @interface LoginUserId {
    /**
     * true(기본값): 인증 정보 필수, 없으면 예외 발생.
     * false: 인증 정보 선택, 없으면 null return.
     * @return
     */
    boolean required() default true;
}
