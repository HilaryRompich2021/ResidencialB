package com.example.Mi.casita.segura.pagos.Bitacora.model;


//import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
//import org.hibernate.annotations.TypeDef;
//import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.type.SqlTypes;
//import org.hibernate.annotations.Type;


@Data
@Entity
public class BitacoraDetallePagoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bitacoraPagoDetalle_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    private BitacoraPagoDetalle bitacoraPagoDetalle;

    @Column(nullable = false, length = 100)
    private String usuario;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = true)
    private String datosAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = true)
    private String datosNuevos;
}
