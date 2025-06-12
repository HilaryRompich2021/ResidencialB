package com.example.Mi.casita.segura.soporte.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO genérico para actualizar estado de un ticket.
 * Se reutiliza para “EN_PROCESO” y “COMPLETADO”.
 */
@Data
public class UpdateEstadoRequestDTO {

    @NotNull
    private Long ticketId;

    @NotBlank
    private String detalle;  // Descripción adicional que se anidará a la descripción histórica
}
