package com.example.Mi.casita.segura.reservas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@Data
public class ReservaListadoDTO {
    private Long id;
    private String areaComun;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;      // p. ej. "RESERVADO"
    private BigDecimal costoTotal;

   /* public ReservaListadoDTO(Long id, String areaComun, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String name, BigDecimal costoTotal) {
    }*/
}
