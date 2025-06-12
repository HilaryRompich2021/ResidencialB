package com.example.Mi.casita.segura.soporte.Bitacora.repository;

import com.example.Mi.casita.segura.soporte.Bitacora.model.BitacoraDetalleTicketSoporte;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraDetalleTicketSoporteRepository extends JpaRepository<BitacoraDetalleTicketSoporte, Long> {
    @Modifying
    @Transactional
    @Query(value = "insert into bitacora_detalle_ticket_soporte " +
            "(bitacora_ticket_soporte_id, datos_anteriores, datos_nuevos, usuario) " +
            "values (?1, CAST(?2 AS jsonb), CAST(?3 AS jsonb), ?4)",
            nativeQuery = true)
    void insertarDetalle(Long bitacoraId, String datosAnteriores, String datosNuevos, String usuario);

}