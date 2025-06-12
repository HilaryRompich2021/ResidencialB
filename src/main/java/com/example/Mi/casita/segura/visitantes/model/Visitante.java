package com.example.Mi.casita.segura.visitantes.model;

import com.example.Mi.casita.segura.acceso.model.Acceso_QR;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Visitante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true)
    private String cui;

    @Column(nullable = false, length = 100)
    private String nombreVisitante;

    private boolean estado;

    private LocalDate fechaDeIngreso;

    @Column(length = 20)
    private String telefono;

    private Integer numeroCasa;

    @Column(length = 100)
    private String motivoVisita;

    @Column(columnDefinition = "TEXT")
    private String nota;

    // Relación con los usuarios que crean visitantes.
    @ManyToOne
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    // Relación uno a uno con el código QR generado
    @OneToOne(mappedBy = "visitante", cascade = CascadeType.ALL)
    private Acceso_QR acceso_QR;

}
