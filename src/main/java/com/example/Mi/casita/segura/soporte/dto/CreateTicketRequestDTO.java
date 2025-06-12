package com.example.Mi.casita.segura.soporte.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketRequestDTO {

    @NotBlank
    private String tipoError;

    @NotBlank
    private String descripcion;

}