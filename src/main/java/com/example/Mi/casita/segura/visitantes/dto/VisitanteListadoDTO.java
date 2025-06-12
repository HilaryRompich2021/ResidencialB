package com.example.Mi.casita.segura.visitantes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitanteListadoDTO {

    private Long id;

    private String cui;

    private String nombreVisitante;

    private boolean estado;

    private LocalDate fechaDeIngreso;

    private String telefono;

    private Integer numeroCasa;

    private String motivoVisita;

    private String nota;

    private String creadoPor;

}
