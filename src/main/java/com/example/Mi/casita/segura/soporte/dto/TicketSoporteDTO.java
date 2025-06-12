package com.example.Mi.casita.segura.soporte.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TicketSoporteDTO {

    private Long id;
    private String tipoError;
    private String descripcion;
    private String estado;
    private LocalDate fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String usuarioCui;
    private String usuarioNombre;
}
