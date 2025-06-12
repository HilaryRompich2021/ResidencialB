package com.example.Mi.casita.segura.correspondencia.model;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class Paquete {

    //Codigo de la empresa de paquetería a guardia
    @Id
    @Column(name = "codigo", nullable = false, length = 36)
    private String codigo;
    /*@Column(name = "codigo_llegada", nullable = false, length = 36)
    private String codigoLlegada;   // A partir de ahora, el nombre Java es “codigoLlegada”

    @Column(name = "codigo_entrega", nullable = false, length = 36, unique = true)
    private String codigoEntrega;*/

    @Column(name = "empresa_de_entrega", nullable = false, length = 100)
    private String empresaDeEntrega;

    @Column(name = "numero_de_guia", nullable = false, length = 50)
    private String numeroDeGuia;

    @Column(name = "tipo_de_paquete", nullable = false, length = 50)
    private String tipoDePaquete;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_recepcion", nullable = true)
    private LocalDateTime fechaRecepcion;

    @Column(name = "fecha_entrega", nullable = true)
    private LocalDateTime fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPaquete estado;

    @ManyToOne
    @JoinColumn(name = "creadoPor")
    private Usuario creadopor;

    public enum EstadoPaquete {
        REGISTRADO,
        PENDIENTE_A_RECOGER,
        ENTREGADO,
        CANCELADO
    }

    public Paquete() {
        // Constructor vacío para JPA
    }


}
