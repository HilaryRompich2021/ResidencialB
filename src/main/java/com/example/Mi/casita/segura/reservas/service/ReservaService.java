package com.example.Mi.casita.segura.reservas.service;

import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import com.example.Mi.casita.segura.pagos.model.Pagos;
import com.example.Mi.casita.segura.pagos.repository.PagosRepository;
import com.example.Mi.casita.segura.reservas.dto.ReservaDTO;
import com.example.Mi.casita.segura.reservas.dto.ReservaListadoDTO;
import com.example.Mi.casita.segura.reservas.model.Reserva;
import com.example.Mi.casita.segura.reservas.repository.ReservaRepository;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepo;
    private final UsuarioRepository usuarioRepo;
    private final PagosRepository pagosRepo;

    public Reserva crearReserva(ReservaDTO dto) {
        Usuario residente = usuarioRepo.findById(dto.getCui())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verifica si la reserva ya existe en esa área, fecha y hora, y está confirmada
        boolean yaReservado = reservaRepo.existsByAreaComunIgnoreCaseAndFechaAndHoraInicioAndHoraFinAndEstado(
                dto.getAreaComun(), dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin(), Reserva.EstadoReserva.RESERVADO);

        if (yaReservado) {
            throw new IllegalArgumentException("Este horario ya fue reservado. Elige otro.");
        }

        //  Calcular costo según área y duración
        Duration duracion = Duration.between(dto.getHoraInicio(), dto.getHoraFin());
        long horas = duracion.toHours();

        // Convertimos a minúsculas y quitamos espacios en los extremos
        String area = dto.getAreaComun().trim().toLowerCase();

        BigDecimal costoTotal = BigDecimal.ZERO;
        switch (area) {
            case "piscina":
                costoTotal = BigDecimal.valueOf(125 * horas);
                break;
            case "cancha":
                costoTotal = BigDecimal.valueOf(125 * horas);
                break;
            case "salón":
            case "salon":
                costoTotal = BigDecimal.valueOf(500 * horas); // Ejemplo: Q500 por 2h
                break;
            default:
                throw new IllegalArgumentException("Área común no válida.");
        }

        // 1. Crear la reserva en estado PENDIENTE
        Reserva reserva = new Reserva();
        reserva.setAreaComun(dto.getAreaComun());
        reserva.setFecha(dto.getFecha());
        reserva.setHoraInicio(dto.getHoraInicio());
        reserva.setHoraFin(dto.getHoraFin());
        reserva.setCostoTotal(costoTotal);
        reserva.setEstado(Reserva.EstadoReserva.PENDIENTE);
        reserva.setResidente(residente);

        Reserva reservaGuardada = reservaRepo.save(reserva);

        // 2. Crear el registro de pago pendiente asociado
        Pagos pago = new Pagos();
        pago.setMontoTotal(costoTotal);
        pago.setMetodoPago("TARJETA");
        pago.setEstado(Pagos.EstadoDelPago.PENDIENTE);
        pago.setFechaPago(LocalDate.now());
        pago.setCreadoPor(residente);

        Pago_Detalle detalle = new Pago_Detalle();
        detalle.setPago(pago);
        detalle.setReserva(reservaGuardada); //  Aquí se asigna el ID de la reserva
        detalle.setMonto(costoTotal);
        detalle.setServicioPagado(Pago_Detalle.ServicioPagado.RESERVA);
        detalle.setEstadoPago(Pago_Detalle.EstadoPago.PENDIENTE);
        detalle.setConcepto("Reserva de " + dto.getAreaComun());
        detalle.setDescripcion("Reserva el " + dto.getFecha() + " de " + dto.getHoraInicio() + " a " + dto.getHoraFin());

        pago.setDetalles(List.of(detalle));
        pagosRepo.save(pago);

        return reservaGuardada;
    }

    /**
     * Obtener reservas activas de un residente (por CUI)
     */
    public List<ReservaListadoDTO> obtenerReservasActivasPorCui(String cui) {
        List<Reserva> listaEntidades = reservaRepo.findByResidenteCuiAndEstado(cui, Reserva.EstadoReserva.RESERVADO);
        return listaEntidades.stream()
                .map(r -> new ReservaListadoDTO(
                        r.getId(),
                        r.getAreaComun(),
                        r.getFecha(),
                        r.getHoraInicio(),
                        r.getHoraFin(),
                        r.getEstado().name(),
                        r.getCostoTotal()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Nuevo: obtener todas las reservas activas ordenadas para el administrador
     */
    /** ADMIN: ahora devolvemos SOLO las reservas confirmadas (estado = RESERVADO), ordenadas */
    public List<ReservaListadoDTO> obtenerReservasConfirmadasOrdenadas() {
        List<Reserva> listaEntidades = reservaRepo.findByEstadoOrderByFechaAscHoraInicioAsc(Reserva.EstadoReserva.RESERVADO);
        return listaEntidades.stream()
                .map(r -> new ReservaListadoDTO(
                        r.getId(),
                        r.getAreaComun(),
                        r.getFecha(),
                        r.getHoraInicio(),
                        r.getHoraFin(),
                        r.getEstado().name(),
                        r.getCostoTotal()
                ))
                .collect(Collectors.toList());
    }

    // 🧠 Método para calcular el costo
    private BigDecimal calcularCosto(String areaComun, LocalTime horaInicio, LocalTime horaFin) {
        long horas = ChronoUnit.HOURS.between(horaInicio, horaFin);
        BigDecimal precioPorHora;

        switch (areaComun.toUpperCase()) {
            case "PISCINA":
                precioPorHora = BigDecimal.valueOf(50);
                break;
            case "SALON":
                precioPorHora = BigDecimal.valueOf(35);
                break;
            case "CANCHA DEPORTIVA":
                precioPorHora = BigDecimal.valueOf(40);
                break;
            default:
                throw new IllegalArgumentException("Área no válida: " + areaComun);
        }

        return precioPorHora.multiply(BigDecimal.valueOf(horas));
    }

}
