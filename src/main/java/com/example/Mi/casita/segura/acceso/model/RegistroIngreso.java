package com.example.Mi.casita.segura.acceso.model;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class RegistroIngreso {

    //PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora_ingreso", nullable = false)
    private LocalDateTime fechaHoraIngreso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ingreso", nullable = false)
    private TipoIngreso tipoIngreso;

    @Column(name = "resultado_validacion", nullable = false)
    private String resultadoValidacion;

    @Column(name = "nombre_lector", nullable = false)
    private String nombreLector;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String observacion;

    // Relación con el Acceso_QR que generó este log
    @ManyToOne
    @JoinColumn(name = "acceso_qr_id", nullable = false)
    private Acceso_QR accesoQr;

   public enum TipoIngreso {
        SISTEMA, PAGO, RESERVA, SEGURIDAD
    }

    @ManyToOne
    @JoinColumn(name="usuario_cui")
    private Usuario usuario;


}
