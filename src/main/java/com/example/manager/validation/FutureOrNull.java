package com.example.manager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureOrNullValidator.class)
@Documented
public @interface FutureOrNull {

    String message() default "Deadline must be in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
