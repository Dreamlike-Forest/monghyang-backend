package com.example.monghyang.domain.global.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Constraint(validatedBy = AllowNullNotBlankStringValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowNullNotBlankString {
    String message() default "빈 문자열이나 공백만 입력할 수 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
