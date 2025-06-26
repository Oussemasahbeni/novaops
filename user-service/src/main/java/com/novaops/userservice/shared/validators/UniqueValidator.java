package com.novaops.userservice.shared.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Optional;

public class UniqueValidator implements ConstraintValidator<UniqueConstraint, Object> {
    // This is a Spring class that allow you to get beans
    private final ApplicationContext applicationContext;
    private String method;
    private Class<?> repository;

    // Spring inject anything you need in this class
    public UniqueValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(UniqueConstraint constraintAnnotation) {
        // we retrieve the data we need from the annotation
        this.method = constraintAnnotation.method();
        this.repository = constraintAnnotation.repository();
    }

    // Here you can put whatever logic you want to execute
    // The first element need to be the type that we will receive, in this case I'll not limit what I
    // can receive
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        // If the value is null we return true,
        // this because this validation is not responsible for checking nulls
        if (value == null) {
            return true;
        }

        try {
            // We get the beans of the repository
            Object instance = applicationContext.getBean(repository);
            // We search the method inside the repository class
            Method callable = ClassUtils.getMethod(repository, method, null);
            // We invoke the method
            Object result = callable.invoke(instance, value);

            if (result instanceof Optional<?> el) {
                return el.isEmpty();
            }
            if (result instanceof Boolean exists) {
                return !exists;
            }
            return result == null;
        } catch (Exception e) {
            // Log the exception if we want, throw it or use a custom exception
            throw new RuntimeException("Error inside UniqueValidator", e);
        }
    }
}
