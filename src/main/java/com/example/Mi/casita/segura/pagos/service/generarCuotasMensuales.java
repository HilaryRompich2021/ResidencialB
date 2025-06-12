package com.example.Mi.casita.segura.pagos.service;

import com.example.Mi.casita.segura.Correo.Service.CorreoService;
import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import com.example.Mi.casita.segura.pagos.model.Pagos;
import com.example.Mi.casita.segura.pagos.repository.PagoDetalleRepository;
import com.example.Mi.casita.segura.pagos.repository.PagosRepository;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class generarCuotasMensuales {

    private final UsuarioRepository usuarioRepo;
    private final PagosRepository pagosRepo;
    private final PagoDetalleRepository pagoDetalleRepo;
    private final CorreoService correoService;


    @Scheduled(cron = "0 0 0 20 * ?") // Cada 20 del mes
   // @Scheduled(cron = "0 * * * * ?")
   // @Scheduled(cron = " * * * * ")
    //@Scheduled(cron = "0 */10 * * * ?")
    //@Scheduled(cron = "0 */5 * * * * ")
    public void generarCuotasMensuales() {
        List<Usuario> residentes = usuarioRepo.findByRol(Usuario.Rol.RESIDENTE);

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes    = hoy.withDayOfMonth(hoy.lengthOfMonth());

        //  LOG para verificar que el método se está ejecutando
        System.out.println("⏳ Ejecutando generación automática de cuota para: " + LocalDate.now());


        for (Usuario residente : residentes) {
           // LocalDate hoy = LocalDate.now();

            /*//Simular más pagos pendientes para la reinstalación
            boolean yaTiene = pagosRepo.existsByCreadoPorAndMesAndAnio(
                    residente.getCui(), hoy.getMonthValue(), hoy.getYear());*/
            boolean yaTiene = pagosRepo.existsByCreadoPorAndFechaPagoBetweenAndEstado(
                    residente.getCui(),
                    inicioMes,
                    finMes
            );


//el if para simular
            if (!yaTiene) {
                Pagos cuota = new Pagos();
                cuota.setMontoTotal(new BigDecimal("550.00"));
                cuota.setFechaPago(hoy); // fecha de emisión
                cuota.setEstado(Pagos.EstadoDelPago.PENDIENTE);
                cuota.setMetodoPago("TARJETA");
                cuota.setCreadoPor(residente);
                Pagos cuotaGuardada = pagosRepo.save(cuota);

                // Crear detalle para que aparezca en el resumen
                Pago_Detalle detalle = new Pago_Detalle();
                detalle.setPago(cuotaGuardada);
                detalle.setConcepto("Cuota mensual de mantenimiento");
                detalle.setDescripcion("Cuota mensual de mantenimiento");
                detalle.setMonto(new BigDecimal("550.00"));
                detalle.setServicioPagado(Pago_Detalle.ServicioPagado.CUOTA);
                detalle.setEstadoPago(Pago_Detalle.EstadoPago.PENDIENTE);

                pagoDetalleRepo.save(detalle); // Guarda el detalle

                // ✅ LOG para cada residente que reciba cuota
                System.out.println("➡️ Cuota creada para: " + residente.getNombre() + " (CUI: " + residente.getCui() + ")");

           }
        }
    }

    /**
     * 2) Cada día a la 01:00 AM, revisa cuántas cuotas PENDIENTES tiene cada residente.
     *    Si un residente acumula ≥ 2 cuotas pendientes, se le envía un correo de recordatorio
     *    usando CorreoService.enviarRecordatorioPago(...).
     */

    @Scheduled(cron = "0 0 1 * * ?") // Todos los días a la 01:00 AM
    //@Scheduled(cron = "0 */10 * * * ?")
    //@Scheduled(cron = "0 */5 * * * * ")
    //@Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void enviarRecordatorioSiTieneDosCuotas() {
        List<Usuario> residentes = usuarioRepo.findByRol(Usuario.Rol.RESIDENTE);
        LocalDate hoy = LocalDate.now();

        // Formateadores para mostrar “Marzo 2025” y “dd/MM/yyyy”
        DateTimeFormatter mesAnioFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        DateTimeFormatter fechaFmt   = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Usuario residente : residentes) {
            // ¿Cuántas cuotas pendientes (detalle de pago tipo CUOTA) tiene?
            int cantidadPendientes = pagosRepo.contarCuotasPendientesPorUsuario(residente.getCui());
            if (cantidadPendientes >= 2 && residente.isEstado()) {
                // Obtenemos todos los detalles pendientes de tipo CUOTA de ese residente
                List<Pago_Detalle> detallesPendientes =
                        pagoDetalleRepo.findByPago_CreadoPor_CuiAndEstadoPago(
                                residente.getCui(),
                                Pago_Detalle.EstadoPago.PENDIENTE
                        );

                for (Pago_Detalle det : detallesPendientes) {
                    LocalDate fechaEmision = det.getPago().getFechaPago();
                    String mesAnio = fechaEmision.format(mesAnioFmt);

                    // Fecha límite: día 21 de ese mes
                    LocalDate fechaLimiteDto = LocalDate.of(
                            fechaEmision.getYear(),
                            fechaEmision.getMonth(),
                            21
                    );
                    String fechaLimite = fechaLimiteDto.format(fechaFmt);

                    // Enviar el correo
                    correoService.enviarRecordatorioPago(
                            residente.getCorreoElectronico(),   // correoDestino
                            residente.getNombre(),             // nombreResidente
                            residente.getNumeroCasa(),         // numeroCasa
                            det.getConcepto(),                  // “Cuota mensual de mantenimiento”
                            mesAnio,                            // “Marzo 2025”
                            det.getMonto(),                     // BigDecimal (550.00)
                            fechaLimite,                        // “21/03/2025”
                            det.getPago().getMetodoPago()       // “TARJETA”
                    );
                    System.out.println("✉️ Enviado recordatorio de pago a: " +
                            residente.getCorreoElectronico() +
                            " | Concepto: " + det.getConcepto() +
                            " | Periodo: " + mesAnio);
                }
            }
        }
    }

  /*  @Transactional
    public void aplicarCorteASuspendidos() {
        List<Usuario> residentes = usuarioRepo.findByRol(Usuario.Rol.RESIDENTE);
        for (Usuario residente : residentes) {
            int cuotasPendientes = pagosRepo.contarCuotasPendientesPorUsuario(residente.getCui());
            if (cuotasPendientes >= 2 && residente.isEstado()) {
                usuarioRepo.desactivarUsuario(residente.getCui());
                System.out.println("Servicio cortado a: " + residente.getNombre());
            }
        }
    }*/
}
