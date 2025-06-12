package com.example.Mi.casita.segura.pagos.model;

import com.example.Mi.casita.segura.pagos.dto.PagoConsultaDTO;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data

public class Pagos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Total pagado, incluyendo cuotas o excedentes
    @Column(nullable = false)
    private BigDecimal montoTotal;

    // Fecha en que se realizó el pago
    @Column(nullable = false)
    private LocalDate fechaPago;

    // Estado del pago: COMPLETADO, PENDIENTE
    @Enumerated(EnumType.STRING)
    private EstadoDelPago estado;

    //Tarjeta
    @Column(length = 30)
    private String metodoPago;

    // Usuario que realizó el pago

    @ManyToOne
    @JoinColumn(name = "creado_por", referencedColumnName = "cui")
    private Usuario creadoPor;

    // Detalle de conceptos individuales dentro del pago
    @OneToMany(
            mappedBy = "pago",
            cascade = CascadeType.ALL,      // aplica todas las operaciones (incl. REMOVE)
            orphanRemoval = true            // elimina detalles “huérfanos”
    )
    @JsonManagedReference
    //@OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    private List<Pago_Detalle> detalles;

    public enum EstadoDelPago {
        COMPLETADO, PENDIENTE

    }


}
