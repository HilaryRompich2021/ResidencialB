package com.example.Mi.casita.segura.soporte.Bitacora.model;

import com.example.Mi.casita.segura.pagos.Bitacora.model.BitacoraDetallePagoDetalle;
import com.example.Mi.casita.segura.soporte.model.TicketSoporte;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class BitacoraTicketSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "TicketSoporte_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    //@JsonBackReference
    private TicketSoporte ticketSoporte;

    @Column(nullable = false, length = 10)
    private String operacion; // CREACION, ACTUALIZACION, ELIMINACION

    @Column(nullable = false)
    private LocalDateTime fecha;

    @OneToMany(
            mappedBy = "bitacoraTicketSoporte",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BitacoraDetalleTicketSoporte> detalles;
}
