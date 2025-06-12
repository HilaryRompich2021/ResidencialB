package com.example.Mi.casita.segura.correspondencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CodigoDTO {
    @NotBlank(message = "El código no puede estar vacío.")
    @Size(min = 36, max = 36, message = "El código debe ser un UUID válido (36 caracteres).")
    private String codigo;

}
