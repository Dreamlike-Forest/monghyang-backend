package com.example.monghyang.domain.global.annotation.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class AllowNullNotBlankStringValidator implements ConstraintValidator<AllowNullNotBlankString, String> {
    private static final Pattern NON_BLANK_PATTERN = Pattern.compile("^\\S.*\\S.*$");
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // null 은 허용
        if (value == null) {
            return true;
        }

        // 정규식: 공백이 아닌 문자가 하나라도 있어야 함
        return NON_BLANK_PATTERN.matcher(value).matches();
    }
}
