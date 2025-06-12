package com.example.Mi.casita.segura.pagos.Bitacora.model;

import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class BitacoraPagoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Pago_Detalle_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Pago_Detalle pagoDetalle;

    @Column(nullable = false, length = 100)
    private String operacion; // CREACION, ACTUALIZACION, ELIMINACION

    @Column(nullable = false)
    private LocalDateTime fecha;

    @OneToMany(
            mappedBy = "bitacoraPagoDetalle",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BitacoraDetallePagoDetalle> detalles;
}
