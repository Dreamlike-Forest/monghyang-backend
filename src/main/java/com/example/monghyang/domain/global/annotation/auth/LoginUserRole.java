package com.example.monghyang.domain.global.annotation.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 적용 범위: 파라메터
@Retention(RetentionPolicy.RUNTIME) // runtime에도 jvm이 읽을 수 있도록 설정
public @interface LoginUserRole {
}
