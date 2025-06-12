package com.example.Mi.casita.segura.soporte.Bitacora.model;

import com.example.Mi.casita.segura.soporte.Bitacora.Datos.JsonAttributeConverter;
//import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Entity
@Data
@Table(name = "bitacora_detalle_ticket_soporte")
public class BitacoraDetalleTicketSoporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bitacoraTicketSoporte_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BitacoraTicketSoporte bitacoraTicketSoporte;

    @Column(nullable = false, length = 100)
    private String usuario;

    @JdbcTypeCode(SqlTypes.JSON)
    //@Column(columnDefinition = "TEXT", nullable = true)
    @Column(name = "datos_anteriores", columnDefinition = "jsonb")
    @Convert(converter = JsonAttributeConverter.class)
    private String datosAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    //@Column(columnDefinition = "TEXT", nullable = true)
    @Column(name = "datos_nuevos", columnDefinition = "jsonb")
    @Convert(converter = JsonAttributeConverter.class)
    private String datosNuevos;

}
