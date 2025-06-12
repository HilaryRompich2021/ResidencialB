package com.example.Mi.casita.segura.pagos.repository;

import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoDetalleRepository extends JpaRepository <Pago_Detalle, Long> {

    // Buscar todos los detalles asociados a un pago específico
    List<Pago_Detalle> findByPagoId(Long pagoId);

    // Buscar detalles de pago por estado (ej: COMPLETADO, PENDIENTE)
    List<Pago_Detalle> findByEstadoPago(String estadoPago);

    // Buscar detalles de pago por tipo de servicio (ej: AGUA, CUOTA)
    List<Pago_Detalle> findByServicioPagado(String servicioPagado);

    List<Pago_Detalle>findByPago_CreadoPor_CuiAndEstadoPago(
            String cui,
            Pago_Detalle.EstadoPago estadoPago
    );

    /**
     * Devuelve todos los detalles de pago de tipo CUOTA PENDIENTE
     * correspondientes a un residente (identificado por su CUI).
     */
    @Query("""
      SELECT pd 
        FROM Pago_Detalle pd
       WHERE pd.pago.creadoPor.cui = :cui
         AND pd.servicioPagado = com.example.Mi.casita.segura.pagos.model.Pago_Detalle.ServicioPagado.CUOTA
         AND pd.estadoPago   = com.example.Mi.casita.segura.pagos.model.Pago_Detalle.EstadoPago.PENDIENTE
    """)
    List<Pago_Detalle> findDetallesDeCuotasPendientesPorUsuario(@Param("cui") String cui);

    /**
     * Verifica si existe al menos un detalle pendiente (cualquier servicio) para un pago específico.
     * Esto lo necesitaremos para saber, tras cerrar un detalle, si quedan otros pendientes.
     */
    boolean existsByPago_IdAndEstadoPago(Long pagoId, Pago_Detalle.EstadoPago estadoPago);


    // Puedes agregar más métodos según necesites

    Optional<Pago_Detalle> findFirstByReserva_IdAndServicioPagadoAndEstadoPago(
            Long reservaId,
            Pago_Detalle.ServicioPagado servicio,
            Pago_Detalle.EstadoPago estado
    );

    Optional<Pago_Detalle> findFirstByReserva_IdAndEstadoPago(Long reservaId,
                                                              Pago_Detalle.EstadoPago estadoPago);

    //Eliminar usuario completo
    //void deleteByCreadoPorCui(String cui);

    List<Pago_Detalle> findByPago_CreadoPor_CuiAndServicioPagadoAndEstadoPago(
            String cui,
            Pago_Detalle.ServicioPagado servicio,
            Pago_Detalle.EstadoPago estado
    );
}


