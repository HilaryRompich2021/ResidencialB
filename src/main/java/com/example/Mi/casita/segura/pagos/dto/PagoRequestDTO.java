package com.example.Mi.casita.segura.pagos.dto;
//RECIBIR PAGOS

import com.example.Mi.casita.segura.pagos.model.Pagos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PagoRequestDTO {
    private BigDecimal montoTotal;
    private String metodoPago; // TARJETA
    private Pagos.EstadoDelPago estado;     // COMPLETADO, PENDIENTE
    private String creadoPor;  // CUI del usuario
    private List<PagoDetalleDTO> detalles;

    // ⚠️ Solo para validación, no guardar en base de datos
    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe tener 16 dígitos")
    private String numeroTarjeta;

    @NotBlank(message = "El CVV es obligatorio")
    @Pattern(regexp = "\\d{3,4}", message = "El CVV debe tener 3 o 4 dígitos")
    private String cvv;

    @NotBlank(message = "La fecha de vencimiento es obligatoria")
    @Pattern(
            regexp = "^(0[1-9]|1[0-2])/\\d{2}$",
            message = "Formato inválido. Usa MM/YY"
    )
    private String fechaVencimiento;

}
