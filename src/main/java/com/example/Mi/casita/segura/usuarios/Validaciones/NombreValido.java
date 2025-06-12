package com.example.Mi.casita.segura.usuarios.Validaciones;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NombreValidoValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface  NombreValido {
    String message() default "El nombre debe tener al menos dos palabras con dos letras o más cada una y solo contener letras";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
