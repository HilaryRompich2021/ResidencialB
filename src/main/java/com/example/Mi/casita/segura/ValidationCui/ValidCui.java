package com.example.Mi.casita.segura.ValidationCui;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CuiValidator.class)
public @interface ValidCui {

    String message() default "CUI inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
