package com.example.Mi.casita.segura.usuarios.model;

import com.example.Mi.casita.segura.acceso.model.Acceso_QR;
import com.example.Mi.casita.segura.correspondencia.model.Paquete;
import com.example.Mi.casita.segura.notificaciones.model.Notificacion;
import com.example.Mi.casita.segura.pagos.model.Pagos;
import com.example.Mi.casita.segura.reservas.model.Reserva;
import com.example.Mi.casita.segura.soporte.model.TicketSoporte;
import com.example.Mi.casita.segura.visitantes.model.Visitante;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

//Representa tabla en base de datos
@Entity
//Lombok
@Data
public class Usuario {

    @Id
    @Column(length = 15)
    private String cui;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String correoElectronico;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false, length = 225)
    private String contrasena;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(nullable = false)
    private LocalDate fechaDeIngreso;

    @Column(nullable = false)
    private boolean estado;

    @Column(nullable = false)
    private int numeroCasa;

    public enum Rol {
        ADMINISTRADOR, RESIDENTE, GUARDIA
    }

    // Relación con los visitantes creados por este usuario
    @OneToMany(mappedBy = "creadoPor", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Visitante> visitantes;

    // Relación con accesos QR generados por este usuario
    @OneToMany(mappedBy = "asociado", cascade = CascadeType.ALL)
    private List<Acceso_QR> acceso_QR;

    // Relación con paquetes ingresados por este usuario
    @OneToMany(mappedBy = "creadopor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Paquete> paquetes;

    // Relación con notificaciones generadas
    @OneToMany(mappedBy = "generadopor", cascade = CascadeType.ALL)
    private List<Notificacion> notificaciones;

    // Relación con pagos realizados
    @OneToMany(mappedBy = "creadoPor", cascade = CascadeType.ALL)
    private List<Pagos> pagos;

    // Relación con reservas hechas
    @OneToMany(mappedBy = "residente", cascade = CascadeType.ALL)
    private List<Reserva> reservas;

    // Relación con tickets de soporte creados
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<TicketSoporte> tickets;
}
