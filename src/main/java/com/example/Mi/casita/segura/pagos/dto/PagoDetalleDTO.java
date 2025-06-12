package com.example.Mi.casita.segura.pagos.dto;

import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import lombok.Data;

import java.math.BigDecimal;

//RECIBIR PAGOS
@Data
public class PagoDetalleDTO {
    private String concepto;
    private String descripcion;
    private BigDecimal monto;
    private Pago_Detalle.ServicioPagado servicioPagado;
    private Pago_Detalle.EstadoPago estadoPago;
    private Long reservaId;       // opcional
    private Long reinstalacionId; // opcional
    private Double metrosCubicosUsados;  // Ej: 7.0

}
