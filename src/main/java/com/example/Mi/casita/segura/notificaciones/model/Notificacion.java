package com.example.Mi.casita.segura.notificaciones.model;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Notificacion {

    //PK
    @Id
    //Permite que la base de datos genere el valor automáticamente
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
   private String titulo;

    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipoNotificacion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @ManyToOne
    @JoinColumn(name = "generadoPor", referencedColumnName = "cui")
    private Usuario generadopor;

    public enum TipoNotificacion {
        SISTEMA, PAGO, RESERVA, SEGURIDAD
    }
}


