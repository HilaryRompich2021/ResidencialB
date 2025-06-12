package com.example.Mi.casita.segura.pagos.model;

import com.example.Mi.casita.segura.pagos.Bitacora.model.BitacoraDetallePagoDetalle;
import com.example.Mi.casita.segura.pagos.Bitacora.model.BitacoraPagoDetalle;
import com.example.Mi.casita.segura.reinstalacion.model.ReinstalacionServicio;
import com.example.Mi.casita.segura.reservas.model.Reserva;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
public class Pago_Detalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String concepto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private ServicioPagado servicioPagado;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago; // Ej: COMPLETADO, PENDIENTE

    //  Relación muchos a uno hacia Pago
    @ManyToOne
    @JoinColumn(name = "pago_id")
    //@JsonIgnore
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Pagos pago;

    //  Relación opcional hacia Reserva (puede ser null)
    @ManyToOne
    @JoinColumn(name = "referencia_reserva_id", nullable = true)
    @JsonIgnore
    private Reserva reserva;

    //  Relación opcional hacia ReinstalacionServicio (puede ser null)
    @ManyToOne
    @JoinColumn(name = "referencia_reinstalacion_id", nullable = true)
    @JsonIgnore
    private ReinstalacionServicio reinstalacion;

    @OneToMany(
            mappedBy = "pagoDetalle",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore  // o @JsonManagedReference si quieres exponerla en JSON controlado
    private List<BitacoraPagoDetalle> bitacora;

    public enum EstadoPago {
        COMPLETADO, PENDIENTE

    }

    public enum ServicioPagado {
         AGUA, LUZ,RESERVA, REINSTALACION,CUOTA

    }
}

