package com.example.Mi.casita.segura.usuarios.Validaciones;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NombreValidoValidator implements ConstraintValidator<NombreValido, String> {

    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.trim().isEmpty()) return false;

        String[] palabras = value.trim().split("\\s+");

        if (palabras.length < 2) return false;

        for (String palabra : palabras) {
            if (!palabra.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}")){
                return false;
            }

        }
        return true;
    }
}
