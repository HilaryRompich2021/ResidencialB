package com.example.Mi.casita.segura.acceso.model;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.visitantes.model.Visitante;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Acceso_QR {

 //PK
 @Id
 //Permite que la base de datos genere el valor automáticamente
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

 @Column(nullable = false)
    private String codigoQR;

 @Column(nullable = false)
 private LocalDateTime fechaGeneracion;

 @Enumerated(EnumType.STRING)
 private Estado estado;

 private LocalDateTime fechaExpiracion; // RN03.4

 //Relacion uno a uno a visitante
 @OneToOne
 //Define la columna en la tabla acceso_qr que referencia a la tabla visitante (FK)
 @JoinColumn(name = "visitante_id", referencedColumnName = "id")
 private Visitante visitante;

 @ManyToOne
 @JoinColumn(name = "asociado_a", referencedColumnName = "cui")
 private Usuario asociado;

 public enum Estado {
  ACTIVO, USADO, EXPIRADO, INACTIVO
          }

  @PrePersist
  public void prePersist() {
   // si no te dieron fechaExpiracion, le asigna 24h después de generación
   if (fechaExpiracion == null && fechaGeneracion != null) {
    fechaExpiracion = fechaGeneracion.plusHours(24);
   }
 }


}

