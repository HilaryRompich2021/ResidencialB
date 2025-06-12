package com.example.Mi.casita.segura.acceso.model.Bitacora;

import com.example.Mi.casita.segura.acceso.model.RegistroIngreso;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class BitacoraRegistroIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "registro_ingreso_id", nullable = false)
    private RegistroIngreso registroIngreso;

    @Column(nullable = false, length = 10)
    private String operacion; // CREACION, ACTUALIZACION, ELIMINACION

    @Column(nullable = false)
    private LocalDateTime fecha;


}
