package com.example.Mi.casita.segura.correspondencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//DTO que el residente envia para registrar un paquete

@Data
public class PaqueteRegistroDTO {
    @NotBlank(message = "Empresa de entrega es obligatoria.")
    @Size(max = 100)
    private String empresaDeEntrega;

    @NotBlank(message = "Número de guía es obligatorio.")
    @Size(max = 50)
    private String numeroDeGuia;

    @NotBlank(message = "Tipo de paquete es obligatorio.")
    @Size(max = 50)
    private String tipoDePaquete;

    @Size(max = 500)
    private String observacion; // opcional, hasta 500 caracteres

}
