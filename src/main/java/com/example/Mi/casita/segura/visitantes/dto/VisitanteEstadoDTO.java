package com.example.Mi.casita.segura.visitantes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VisitanteEstadoDTO {
    @NotNull
    private Boolean estado;
}
