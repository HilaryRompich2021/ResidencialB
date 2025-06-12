package com.example.Mi.casita.segura.correspondencia.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaqueteResponseDTO {
    private String codigo;
    private String empresaDeEntrega;
    private String numeroDeGuia;
    private String tipoDePaquete;
    private String observacion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaRecepcion;
    private LocalDateTime fechaEntrega;
    private String estado;

    // Solo datos concretos del usuario, no toda la entidad Usuario entera
    private String creadoPorCui;
    private String creadoPorUsuario;
    private String creadoPorNombre;
    private Integer creadoPorNumeroCasa;
}
