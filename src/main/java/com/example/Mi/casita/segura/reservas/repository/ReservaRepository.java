package com.example.Mi.casita.segura.reservas.repository;

import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import com.example.Mi.casita.segura.reservas.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByAreaComunIgnoreCaseAndFechaAndHoraInicioAndHoraFinAndEstado(
            String areaComun,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Reserva.EstadoReserva estado
    );

    // --------------------------------------------------------------
    // Nuevo: traer todas las reservas activas (ESTADO = RESERVADO)
    //       de un residente dado (por CUI)
    List<Reserva> findByResidenteCuiAndEstado(String cui, Reserva.EstadoReserva estado);

    // -------------------------------------------------------------
    // Nuevo: traer todas las reservas con estado = RESERVADO,
    // ordenadas por fecha (asc) y horaInicio (asc)
    List<Reserva> findByEstadoOrderByFechaAscHoraInicioAsc(Reserva.EstadoReserva estado);

    //Eliminar usuario
    void deleteByResidente_Cui(String cui);

}


