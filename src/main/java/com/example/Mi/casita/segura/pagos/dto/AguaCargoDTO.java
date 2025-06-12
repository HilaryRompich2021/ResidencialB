package com.example.Mi.casita.segura.pagos.dto;

import lombok.Data;

@Data
public class AguaCargoDTO {
    /**
     * CUI del residente al que se le va a cargar el consumo de agua.
     */
    private String cui;

    /**
     * Metros cúbicos usados por el residente en el mes.
     */
    private Double metrosCubicosUsados;

    /**
     * (Opcional) Si se desea registrar inmediatamente como “completado” o dejar pendiente.
     * Normalmente será PENDIENTE.
     */
    private String estadoPago; // Ej. "PENDIENTE"
}
