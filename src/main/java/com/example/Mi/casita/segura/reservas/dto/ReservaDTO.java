package com.example.Mi.casita.segura.reservas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

//DTO para la creación de reservas
@Data
public class ReservaDTO {
    private String cui;
    private String areaComun;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

}

