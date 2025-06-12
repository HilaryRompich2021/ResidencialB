package com.example.Mi.casita.segura.reservas.model;

import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Área común reservada: salón, cancha, piscina, etc.
    @Column(nullable = false)
    private String areaComun;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    // Usuario que reservó (residente)
    @ManyToOne
    @JoinColumn(name = "asociado_a", referencedColumnName = "cui")
    private Usuario residente;


    @Column(nullable = false)
    private BigDecimal costoTotal;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    public enum EstadoReserva {
        PENDIENTE,
        RESERVADO,
        INACTIVO
    }


   /* @OneToMany(mappedBy = "reserva")
    private List<Pago_Detalle> detallesPago;*/

}
