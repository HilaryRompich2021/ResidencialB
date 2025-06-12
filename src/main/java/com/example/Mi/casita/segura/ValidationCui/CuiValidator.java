package com.example.Mi.casita.segura.ValidationCui;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CuiValidator implements ConstraintValidator<ValidCui, String> {

    @Override
    public boolean isValid(String cui, ConstraintValidatorContext context) {
        if (cui == null || !cui.matches("\\d{13}")) return false;

        int[] munisPorDepto = {17, 8, 16, 16, 13, 14, 19, 8, 24, 21, 9,
                30, 32, 21, 8, 17, 14, 5, 11, 11, 7, 17};

        int depto = Integer.parseInt(cui.substring(9, 11));
        int muni = Integer.parseInt(cui.substring(11, 13));

        if (depto == 0 || muni == 0 || depto > munisPorDepto.length || muni > munisPorDepto[depto - 1]) {
            return false;
        }

        String numero = cui.substring(0, 8);
        int verificador = Integer.parseInt(cui.substring(8, 9));
        int total = 0;

        for (int i = 0; i < numero.length(); i++) {
            total += Integer.parseInt(numero.charAt(i) + "") * (i + 2);
        }

        return total % 11 == verificador;
    }
}
