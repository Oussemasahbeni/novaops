package com.novaops.notificationservice.shared.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.stereotype.Service;

@Target(TYPE)
@Retention(RUNTIME)
@Service
public @interface Utility {}
