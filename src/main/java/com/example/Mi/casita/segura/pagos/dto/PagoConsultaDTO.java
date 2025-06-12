package com.example.Mi.casita.segura.pagos.dto;

import com.example.Mi.casita.segura.pagos.model.Pagos;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PagoConsultaDTO {

    private Long id;
    private BigDecimal montoTotal;
    private String metodoPago;
    private Pagos.EstadoDelPago estado;
    private LocalDate fechaPago;
    private List<PagoDetalleConsultaDTO> detalles;
}
