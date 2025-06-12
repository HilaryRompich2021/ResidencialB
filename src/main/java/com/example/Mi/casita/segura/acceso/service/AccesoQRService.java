package com.example.Mi.casita.segura.acceso.service;

import com.example.Mi.casita.segura.acceso.model.Acceso_QR;
import com.example.Mi.casita.segura.acceso.model.RegistroIngreso;
import com.example.Mi.casita.segura.acceso.model.Bitacora.BitacoraRegistroIngreso;
import com.example.Mi.casita.segura.acceso.model.Bitacora.BitacoraDetalleRegistroIngreso;
import com.example.Mi.casita.segura.acceso.repository.AccesoQRRepository;
import com.example.Mi.casita.segura.acceso.repository.BitacoraDetalleRegistroIngresoRepository;
import com.example.Mi.casita.segura.acceso.repository.RegistroIngresoRepository;
import com.example.Mi.casita.segura.acceso.repository.BitacoraRegistroIngresoRepository;
import com.example.Mi.casita.segura.acceso.websocket.TalanqueraWebSocketHandler;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AccesoQRService {

    private final AccesoQRRepository repo;
    private final RegistroIngresoRepository logRepo;
    private final BitacoraRegistroIngresoRepository bitacoraRegistroRepo;
    private final BitacoraDetalleRegistroIngresoRepository bitacoraDetalleRepo;
    private final TalanqueraWebSocketHandler ws;

    @Transactional
    public void borrarAccesosDeUsuario(String cui) {
        repo.deleteByAsociado_Cui(cui);
    }

    public AccesoQRService(AccesoQRRepository repo,
                           RegistroIngresoRepository logRepo,
                           BitacoraRegistroIngresoRepository bitacoraRegistroRepo,
                           BitacoraDetalleRegistroIngresoRepository bitacoraDetalleRepo,
                           TalanqueraWebSocketHandler ws) {
        this.repo = repo;
        this.logRepo = logRepo;
        this.bitacoraRegistroRepo = bitacoraRegistroRepo;
        this.bitacoraDetalleRepo = bitacoraDetalleRepo;
        this.ws = ws;
    }

    public void procesarCodigo(String qr, boolean esEntrada) {
        // 1) Buscamos el QR
        Optional<Acceso_QR> opt = repo.findByCodigoQR(qr);
        if (!opt.isPresent()) {
            ws.broadcast("deny");
            // no podemos bitacorar un QR inexistente (no hay entidad Acceso_QR)
            return;
        }
        Acceso_QR acceso = opt.get();

        // 2) Rechazo si no está ACTIVO
        if (acceso.getEstado() != Acceso_QR.Estado.ACTIVO) {
            ws.broadcast("deny");
            registrarDenegado(acceso, "NO_ACTIVO");
            return;
        }

        // 3) Rutas de entrada/salida
        if (esEntrada) {
            ws.broadcast("open_entry");
            RegistroIngreso log = logRepo.save(
                    crearLog(acceso, RegistroIngreso.TipoIngreso.SISTEMA,
                            acceso.getVisitante() != null ? "Entrada visitante" : "Entrada residente")
            );
            registrarExito(log, acceso, "ENTRADA");
        } else {
            if (acceso.getVisitante() != null) {
                long conteo = logRepo.countByAccesoQrIdAndTipoIngreso(
                        acceso.getId(), RegistroIngreso.TipoIngreso.SISTEMA
                );
                if (conteo == 0) {
                    ws.broadcast("deny");
                    registrarDenegado(acceso, "SALIDA_SIN_ENTRADA");
                    return;
                }
            }
            ws.broadcast("open_exit");
            // Para visitante, además inactivamos el QR
            if (acceso.getVisitante() != null) {
                acceso.setEstado(Acceso_QR.Estado.INACTIVO);
                repo.save(acceso);
            }
            RegistroIngreso log = logRepo.save(
                    crearLog(acceso, RegistroIngreso.TipoIngreso.SISTEMA,
                            acceso.getVisitante() != null ? "Salida visitante" : "Salida residente")
            );
            registrarExito(log, acceso, "SALIDA");
        }
    }

    private RegistroIngreso crearLog(Acceso_QR acceso,
                                     RegistroIngreso.TipoIngreso tipo,
                                     String obs) {
        RegistroIngreso log = new RegistroIngreso();
        log.setAccesoQr(acceso);
        log.setFechaHoraIngreso(LocalDateTime.now());
        log.setTipoIngreso(tipo);
        log.setResultadoValidacion("OK");
        log.setNombreLector("WebCam");
        log.setObservacion(obs);
        return log;
    }

    /** Bitacora de operaciones exitosas */
    private void registrarExito(RegistroIngreso log,
                                Acceso_QR acceso,
                                String operacion) {
        // 1) Cabecera
        BitacoraRegistroIngreso br = new BitacoraRegistroIngreso();
        br.setRegistroIngreso(log);
        br.setOperacion(operacion);
        br.setFecha(LocalDateTime.now());
        bitacoraRegistroRepo.save(br);

        // 2) Detalle
        BitacoraDetalleRegistroIngreso det = new BitacoraDetalleRegistroIngreso();
        det.setBitacoraRegistroIngreso(br);
        det.setUsuario(
                acceso.getVisitante() != null
                        ? acceso.getVisitante().getNombreVisitante()
                        : acceso.getAsociado().getUsuario()
        );
        det.setDatosAnteriores("{\"estado\":\"" + acceso.getEstado() + "\"}");
        det.setDatosNuevos("{\"operacion\":\"" + operacion + "\"}");
        bitacoraDetalleRepo.save(det);
    }

    /** Bitacora de operaciones denegadas */
    private void registrarDenegado(Acceso_QR acceso, String motivo) {
        // Creamos un RegistroIngreso “virtual” para el rechazo
        RegistroIngreso log = new RegistroIngreso();
        log.setAccesoQr(acceso);
        log.setFechaHoraIngreso(LocalDateTime.now());
        log.setTipoIngreso(RegistroIngreso.TipoIngreso.SISTEMA);
        log.setResultadoValidacion("DENIED");
        log.setNombreLector("WebCam");
        log.setObservacion(motivo);
        logRepo.save(log);

        // Luego lo registramos en bitácora como “DENEGADO”
        registrarExito(log, acceso, "DENEGADO");
    }
}
