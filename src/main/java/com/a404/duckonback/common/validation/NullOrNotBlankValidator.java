package com.a404.duckonback.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // patch시 null 허용
        if(value == null) return true;

        // " " or "   " 공백만 있는 경우 false 반환
        return !value.trim().isEmpty();
    }
}
