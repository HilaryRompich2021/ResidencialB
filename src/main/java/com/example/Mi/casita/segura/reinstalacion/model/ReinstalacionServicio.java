package com.example.Mi.casita.segura.reinstalacion.model;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
public class ReinstalacionServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //  Fecha en que solicita la reinstalación
    @Column(nullable = false)
    private LocalDate fecha_solicitud;

    // Monto a pagar por la reinstalación (Q89)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(length = 20, nullable = false)
    private String estado;

    // Usuario afectado (residente fk)
    @ManyToOne
    @JoinColumn(name = "cui_usuario", referencedColumnName = "cui")
    @JsonIgnore
    private Usuario usuario;



}
