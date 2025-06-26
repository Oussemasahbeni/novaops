package com.novaops.userservice.shared.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * This annotation is used to validate if a value is unique in a repository Example
 * usage: @UniqueConstraint(method = "findByUsername", repository = UserRepository.class)
 */
@Documented
@Constraint(validatedBy = UniqueValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueConstraint {
    String message() default "The value already exists";

    String method();

    Class<?> repository();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
