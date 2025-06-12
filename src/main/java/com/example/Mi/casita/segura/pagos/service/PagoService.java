package com.example.Mi.casita.segura.pagos.service;

import com.example.Mi.casita.segura.Correo.Service.CorreoService;
import com.example.Mi.casita.segura.pagos.Bitacora.CapturaDatos.JsonUtil;
import com.example.Mi.casita.segura.pagos.Bitacora.service.PagoDetalleBitacoraService;
import com.example.Mi.casita.segura.pagos.dto.*;
import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import com.example.Mi.casita.segura.pagos.model.Pagos;
import com.example.Mi.casita.segura.pagos.repository.PagoDetalleRepository;
import com.example.Mi.casita.segura.pagos.repository.PagosRepository;
import com.example.Mi.casita.segura.reinstalacion.model.ReinstalacionServicio;
import com.example.Mi.casita.segura.reservas.model.Reserva;
import com.example.Mi.casita.segura.reservas.repository.ReservaRepository;
import com.example.Mi.casita.segura.reinstalacion.repository.ReinstalacionRepository;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    private final PagosRepository pagosRepo;
    private final UsuarioRepository usuarioRepo;
    private final ReservaRepository reservaRepo;
    private final ReinstalacionRepository reinstalacionRepo;
    private final PagoDetalleRepository pagoDetalleRepo;
    private final CorreoService correoService;
    private final PagoDetalleBitacoraService detalleBitacoraService;
    private final JsonUtil jsonUtil; // Para serializar objetos a JSON si hace falta


    @Transactional
    public Pagos registrarPago(PagoRequestDTO dto) {
        // 1) Recuperar usuario creador
        Usuario usuario = usuarioRepo.findById(dto.getCreadoPor())
                .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado"));

        // 2) Contar únicamente las CUOTAS pendientes (servicio = CUOTA) para este usuario
        int cuotasPendientesPrevias = pagosRepo.contarCuotasPendientesPorUsuario(dto.getCreadoPor());

        // 3) Construir el objeto Pagos
        Pagos pago = new Pagos();
        pago.setMontoTotal(dto.getMontoTotal());       // Luego se ajustará si hay reinstalación
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago(dto.getMetodoPago());
        pago.setEstado(dto.getEstado());               // normalmente "COMPLETADO"
        pago.setCreadoPor(usuario);

        // 4) Validar datos de tarjeta (si aplica)
        validarTarjeta(dto);

        // 5) Crear la lista de detalles de este pago
        List<Pago_Detalle> detalles = new ArrayList<>();
        BigDecimal montoTotal = BigDecimal.ZERO;

        for (PagoDetalleDTO detDTO : dto.getDetalles()) {
            Pago_Detalle detalle = new Pago_Detalle();
            detalle.setConcepto(detDTO.getConcepto());
            detalle.setDescripcion(detDTO.getDescripcion());
            detalle.setServicioPagado(detDTO.getServicioPagado());
            detalle.setEstadoPago(detDTO.getEstadoPago());
            detalle.setPago(pago);  // asociarlo al pago aún no guardado

            // 5.1) Si trae reservaId, asignar la reserva
            if (detDTO.getReservaId() != null) {
                Reserva reserva = reservaRepo.findById(detDTO.getReservaId())
                        .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
                detalle.setReserva(reserva);
            }

            // 5.2) Si trae reinstalacionId, asignar la reinstalación
            if (detDTO.getReinstalacionId() != null) {
                ReinstalacionServicio reinstalacion = reinstalacionRepo.findById(detDTO.getReinstalacionId())
                        .orElseThrow(() -> new IllegalArgumentException("Reinstalación no encontrada"));
                detalle.setReinstalacion(reinstalacion);
            }

            // 5.3) Si es AGUA, calcular excedente y monto
            if (detDTO.getServicioPagado() == Pago_Detalle.ServicioPagado.AGUA) {
                double usados = detDTO.getMetrosCubicosUsados() != null ? detDTO.getMetrosCubicosUsados() : 0.0;
                double excedente = usados > 4.0 ? usados - 4.0 : 0.0;
                BigDecimal montoExceso = BigDecimal.valueOf(excedente * 23.50);
                BigDecimal cuotaBase = new BigDecimal("550.00");
                detalle.setMonto(cuotaBase.add(montoExceso));
                detalle.setDescripcion("Consumo de agua: " + usados + " m³, Excedente: " + excedente + " m³");
            } else {
                // Para CUOTA, RESERVA, REINSTALACION… usar el monto directo
                detalle.setMonto(detDTO.getMonto());
            }

            montoTotal = montoTotal.add(detalle.getMonto());
            detalles.add(detalle);
        }

        // 6) Si hay ≥ 2 CUOTAS pendientes previas, agregar cargo de reinstalación
        if (cuotasPendientesPrevias >= 2) {
            ReinstalacionServicio reinstalacion = new ReinstalacionServicio();
            reinstalacion.setUsuario(usuario);
            reinstalacion.setFecha_solicitud(LocalDate.now());
            reinstalacion.setEstado("PENDIENTE");
            reinstalacion.setMonto(new BigDecimal("89.00"));
            reinstalacion = reinstalacionRepo.save(reinstalacion);

            Pago_Detalle cargoReinstalacion = new Pago_Detalle();
            cargoReinstalacion.setConcepto("Reinstalación de servicio");
            cargoReinstalacion.setDescripcion("Cargo adicional por 2 meses o más en atraso");
            cargoReinstalacion.setMonto(new BigDecimal("89.00"));
            cargoReinstalacion.setServicioPagado(Pago_Detalle.ServicioPagado.REINSTALACION);
            cargoReinstalacion.setEstadoPago(Pago_Detalle.EstadoPago.COMPLETADO);
            cargoReinstalacion.setPago(pago);
            cargoReinstalacion.setReinstalacion(reinstalacion);

            montoTotal = montoTotal.add(cargoReinstalacion.getMonto());
            detalles.add(cargoReinstalacion);
        }

        // 7) Fijar monto total ajustado y asignar detalles al pago
        pago.setMontoTotal(montoTotal);
        pago.setDetalles(detalles);

        // 8) Guardar el pago (cascade guardará también los detalles)
        Pagos pagoGuardado = pagosRepo.save(pago);

        //BITACORA_CREACION REGISTRO
        String usuarioLog = usuario.getCui(); // o el nombre de usuario real
        for (Pago_Detalle cadaDetalle : detalles) {
            // Llamada al servicio de bitácora para CREAR ese detalle:
            detalleBitacoraService.crearConBitacora(cadaDetalle, usuarioLog);
        }



        // 9) ——— MARCAR COMO “COMPLETADOS” LOS DETALLES DE CUOTA PENDIENTES ANTERIORES ———
        // Recuperar todos los detalles PENDIENTES de tipo CUOTA para este usuario
        List<Pago_Detalle> detallesPendientesCuota = pagoDetalleRepo
                //.findDetallesDeCuotasPendientesPorUsuario(
                .findByPago_CreadoPor_CuiAndServicioPagadoAndEstadoPago(
                        usuario.getCui(),
                        Pago_Detalle.ServicioPagado.AGUA,
                        Pago_Detalle.EstadoPago.PENDIENTE
                );
        for (Pago_Detalle detPend : detallesPendientesCuota) {
            // 9.1) Cambiar el estado a COMPLETADO y guardar el detalle
            detPend.setEstadoPago(Pago_Detalle.EstadoPago.COMPLETADO);
            pagoDetalleRepo.save(detPend);

            // 9.2) Crear una NUEVA bitácora para este detalle ya completado
            detalleBitacoraService.crearConBitacora(detPend, usuarioLog);

            // 9.3) Verificar si el pago padre ya no tiene detalles pendientes
            Pagos pagoPadre = detPend.getPago();
            boolean quedanPendientes = pagoDetalleRepo
                    .existsByPago_IdAndEstadoPago(pagoPadre.getId(), Pago_Detalle.EstadoPago.PENDIENTE);

            // 9.4) Si ya no quedan pendientes y el pago está PENDIENTE, marcar el pago como COMPLETADO
            if (!quedanPendientes
                    && pagoPadre.getEstado() == Pagos.EstadoDelPago.PENDIENTE) {
                pagoPadre.setEstado(Pagos.EstadoDelPago.COMPLETADO);
                pagosRepo.save(pagoPadre);
            }
        }

/*
                for (Pago_Detalle detPend : detallesPendientesCuota) {
                    Pago_Detalle cambios = new Pago_Detalle();
                    cambios.setConcepto(detPend.getConcepto());
                    cambios.setDescripcion(detPend.getDescripcion());
                    cambios.setMonto(detPend.getMonto());
                    cambios.setServicioPagado(detPend.getServicioPagado());
                    cambios.setEstadoPago(Pago_Detalle.EstadoPago.COMPLETADO);
                    cambios.setPago(detPend.getPago());
                    cambios.setReserva(detPend.getReserva());
                    cambios.setReinstalacion(detPend.getReinstalacion());
                    // Nota: el objeto “cambios” debe contener todos los campos (o al menos los que cambian),
                    // luego se copiarán en el método actualizarConBitacora.
                    detalleBitacoraService.actualizarConBitacora(detPend.getId(), cambios, usuarioLog);

                    detPend.setEstadoPago(Pago_Detalle.EstadoPago.COMPLETADO);
                    pagoDetalleRepo.save(detPend);


            // 9.2) Verificar si el pago padre (detPend.getPago()) aún tiene otros detalles pendientes
            Pagos pagoPadre = detPend.getPago();
            boolean quedanPendientes = pagoDetalleRepo
                    .existsByPago_IdAndEstadoPago(pagoPadre.getId(), Pago_Detalle.EstadoPago.PENDIENTE);

            // 9.3) Si el pago padre ya no tiene detalles pendientes, marcarlo también como COMPLETADO
            if (!quedanPendientes && pagoPadre.getEstado() == Pagos.EstadoDelPago.PENDIENTE) {
                pagoPadre.setEstado(Pagos.EstadoDelPago.COMPLETADO);
                pagosRepo.save(pagoPadre);
            }
        }*/

        // 10) ——— MARCAR COMO “COMPLETADOS” LOS DETALLES DE RESERVA PENDIENTES ———
        for (PagoDetalleDTO detDTO : dto.getDetalles()) {
            if (detDTO.getReservaId() != null) {
                Optional<Pago_Detalle> optDetallePendiente = pagoDetalleRepo
                        .findFirstByReserva_IdAndEstadoPago(detDTO.getReservaId(), Pago_Detalle.EstadoPago.PENDIENTE);

                if (optDetallePendiente.isPresent()) {
                    Pago_Detalle detalleReservaPend = optDetallePendiente.get();

                    Pago_Detalle cambios = new Pago_Detalle();
                    cambios.setConcepto(detalleReservaPend.getConcepto());
                    cambios.setDescripcion(detalleReservaPend.getDescripcion());
                    cambios.setMonto(detalleReservaPend.getMonto());
                    cambios.setServicioPagado(detalleReservaPend.getServicioPagado());
                    cambios.setEstadoPago(Pago_Detalle.EstadoPago.COMPLETADO);
                    cambios.setPago(detalleReservaPend.getPago());
                    cambios.setReserva(detalleReservaPend.getReserva());
                    cambios.setReinstalacion(detalleReservaPend.getReinstalacion());
                    detalleBitacoraService.actualizarConBitacora(detalleReservaPend.getId(), cambios, usuarioLog);


                   /* detalleReservaPend.setEstadoPago(Pago_Detalle.EstadoPago.COMPLETADO);
                    pagoDetalleRepo.save(detalleReservaPend);*/

                    Pagos pagoPadreReserva = detalleReservaPend.getPago();
                    boolean quedanPendientesParaReserva = pagoDetalleRepo
                            .existsByPago_IdAndEstadoPago(pagoPadreReserva.getId(), Pago_Detalle.EstadoPago.PENDIENTE);

                    if (!quedanPendientesParaReserva && pagoPadreReserva.getEstado() == Pagos.EstadoDelPago.PENDIENTE) {
                        pagoPadreReserva.setEstado(Pagos.EstadoDelPago.COMPLETADO);
                        pagosRepo.save(pagoPadreReserva);
                    }
                }
            }
        }

        // 11) Finalmente, confirmar la reserva en la tabla “Reserva” y enviar correo
        for (PagoDetalleDTO detDTO : dto.getDetalles()) {
            if (detDTO.getReservaId() != null) {
                Reserva reserva = reservaRepo.findById(detDTO.getReservaId())
                        .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada para actualizar"));

                reserva.setEstado(Reserva.EstadoReserva.RESERVADO);
                Reserva reservaActualizada = reservaRepo.save(reserva);

                // Preparar datos para el correo
                String fechaFormateada = reservaActualizada.getFecha()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String horaInicioStr = reservaActualizada.getHoraInicio()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
                String horaFinStr = reservaActualizada.getHoraFin()
                        .format(DateTimeFormatter.ofPattern("HH:mm"));

                // Intentar enviar correo, pero capturar excepción para que el pago no falle
                try {
                    correoService.enviarConfirmacionReserva(
                            usuario.getCorreoElectronico(),
                            usuario.getNombre(),
                            usuario.getNumeroCasa(),
                            reservaActualizada.getAreaComun(),
                            fechaFormateada,
                            horaInicioStr,
                            horaFinStr
                    );
                } catch (MailSendException ex) {
                    logger.error("Error enviando correo de confirmación de reserva para el usuario {}: {}",
                            usuario.getCui(), ex.getMessage());
                }
            }
        }


        return pagoGuardado;
    }



    private void validarTarjeta(PagoRequestDTO dto) {
        if (!dto.getMetodoPago().equalsIgnoreCase("TARJETA")) return;

        if (!dto.getNumeroTarjeta().matches("\\d{16}"))
            throw new IllegalArgumentException("El número de tarjeta debe tener 16 dígitos");

        if (!dto.getCvv().matches("\\d{3,4}"))
            throw new IllegalArgumentException("El CVV debe tener 3 o 4 dígitos");

        if (!dto.getFechaVencimiento().matches("^(0[1-9]|1[0-2])/\\d{2}$"))
            throw new IllegalArgumentException("La fecha de vencimiento debe tener formato MM/YY");
    }

    /**
     * Genera un cargo de agua en estado PENDIENTE para un residente.
     * Calcula el exceso (metros – 4) y suma al monto base (550 Q).
     */
    @Transactional
    public Pagos generarCargoAgua(AguaCargoDTO dto) {
        Usuario residente = usuarioRepo.findById(dto.getCui())
                .orElseThrow(() -> new IllegalArgumentException("Residente no encontrado con CUI " + dto.getCui()));

        double usados = dto.getMetrosCubicosUsados() != null ? dto.getMetrosCubicosUsados() : 0.0;
        double exceso = usados > 4.0 ? usados - 4.0 : 0.0;

        BigDecimal cuotaBase = new BigDecimal("550.00");
        BigDecimal montoExceso = BigDecimal.valueOf(exceso * 23.50);
        BigDecimal montoTotal = cuotaBase.add(montoExceso);

        Pagos pago = new Pagos();
        pago.setCreadoPor(residente);
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago("AGUA");
        pago.setEstado(Pagos.EstadoDelPago.PENDIENTE);
        pago.setMontoTotal(montoTotal);

        Pago_Detalle detalleAgua = new Pago_Detalle();
        detalleAgua.setPago(pago);
        detalleAgua.setServicioPagado(Pago_Detalle.ServicioPagado.AGUA);
        detalleAgua.setEstadoPago(Pago_Detalle.EstadoPago.PENDIENTE);

        String descripcion = String.format(
                "Consumo de agua: %.2f m³ (Excedente: %.2f m³). Monto: Q%.2f (Q550 base + Q%.2f exceso)",
                usados, exceso, montoTotal.doubleValue(), montoExceso.doubleValue()
        );
        detalleAgua.setDescripcion(descripcion);
        detalleAgua.setConcepto("Cargo mensual de agua");
        detalleAgua.setMonto(montoTotal);

        Pagos pagoGuardado = pagosRepo.save(pago);
        detalleAgua.setPago(pagoGuardado);

        Pago_Detalle guardadoAgua = pagoDetalleRepo.save(detalleAgua);
        detalleBitacoraService.crearConBitacora(guardadoAgua, residente.getCui());
        //pagoDetalleRepo.save(detalleAgua);

        return pagoGuardado;
    }


    public List<Pagos> obtenerPagosPorUsuario(String cui) {
        List<Pagos> pagos = pagosRepo.findByCreadoPorCui(cui);

        LocalDate hoy = LocalDate.now();
        LocalDate fechaCorte = LocalDate.of(hoy.getYear(), hoy.getMonth(), 21);

        // Verifica si hay algún pago COMPLETADO en el mes actual
        boolean cuotaPagada = pagos.stream()
                .anyMatch(p -> p.getFechaPago().getMonth() == hoy.getMonth()
                        && p.getEstado() == Pagos.EstadoDelPago.COMPLETADO);

        // Si no hay, crea una cuota pendiente
        if (!cuotaPagada && hoy.isAfter(fechaCorte)) {
            Pagos cuotaPendiente = new Pagos();
            cuotaPendiente.setMontoTotal(BigDecimal.valueOf(550));
            cuotaPendiente.setFechaPago(fechaCorte);
            cuotaPendiente.setEstado(Pagos.EstadoDelPago.PENDIENTE);
            Usuario usuario = usuarioRepo.findById(cui).orElse(null);
            cuotaPendiente.setCreadoPor(usuario);
            pagos.add(cuotaPendiente);
        }

        return pagos;
    }

    public List<PagoConsultaDTO> obtenerPagosPorCui(String cui) {
        List<Pagos> pagos = pagosRepo.findByCreadoPorCui(cui);

        return pagos.stream().map(pago -> {
            PagoConsultaDTO dto = new PagoConsultaDTO();
            dto.setId(pago.getId());
            dto.setMontoTotal(pago.getMontoTotal());
            dto.setMetodoPago(pago.getMetodoPago());
            dto.setEstado(pago.getEstado());
            dto.setFechaPago(pago.getFechaPago());

            // Mapear detalles
            List<PagoDetalleConsultaDTO> detalleDTOs = pago.getDetalles().stream().map(detalle -> {
                PagoDetalleConsultaDTO detalleDTO = new PagoDetalleConsultaDTO();
                detalleDTO.setConcepto(detalle.getConcepto());
                detalleDTO.setDescripcion(detalle.getDescripcion());
                detalleDTO.setMonto(detalle.getMonto());
                detalleDTO.setServicioPagado(detalle.getServicioPagado());
                detalleDTO.setEstadoPago(detalle.getEstadoPago());

                // si este Pago_Detalle tiene reserva asociada, setea el id:
                if (detalle.getReserva() != null) {

                    detalleDTO.setReservaId(detalle.getReserva().getId());
                }

                return detalleDTO;


            }).collect(Collectors.toList());

            dto.setDetalles(detalleDTOs);

            return dto;
        }).collect(Collectors.toList());
    }


}
