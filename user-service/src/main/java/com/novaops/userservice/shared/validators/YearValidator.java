package com.novaops.userservice.shared.validators;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Year;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = YearValidator.CurrentYearValidator.class)
public @interface YearValidator {
    String message() default "Year cannot be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class CurrentYearValidator implements ConstraintValidator<YearValidator, Integer> {
        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            int currentYear = Year.now().getValue();
            return value <= currentYear;
        }
    }
}