package com.example.Mi.casita.segura.pagos.Bitacora.service;

import com.example.Mi.casita.segura.pagos.Bitacora.CapturaDatos.JsonUtil;
import com.example.Mi.casita.segura.pagos.Bitacora.model.BitacoraDetallePagoDetalle;
import com.example.Mi.casita.segura.pagos.Bitacora.model.BitacoraPagoDetalle;
import com.example.Mi.casita.segura.pagos.Bitacora.repository.BitacoraDetalle_PagoDetalleRepository;
import com.example.Mi.casita.segura.pagos.Bitacora.repository.BitacoraPagoDetalleRepository;
import com.example.Mi.casita.segura.pagos.model.Pago_Detalle;
import com.example.Mi.casita.segura.pagos.repository.PagoDetalleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PagoDetalleBitacoraService {

    private final BitacoraDetalle_PagoDetalleRepository bitacoraDetalleRepo;
    private final BitacoraPagoDetalleRepository bitacoraPagoDetalleRepo;
    private final PagoDetalleRepository pagoDetalleRepo;
    private final JsonUtil jsonUtil;

    //Bitacora de tipo creación
    @Transactional
    public Pago_Detalle crearConBitacora(Pago_Detalle nuevoDetalle, String usuarioQueLoCrea) {

        //Guarda el pago detalle
        Pago_Detalle guardado = pagoDetalleRepo.save(nuevoDetalle);

        //Registro de bitacora
        BitacoraPagoDetalle bitPago = new BitacoraPagoDetalle();
        bitPago.setPagoDetalle(guardado);
        bitPago.setOperacion("CREACION");
        bitPago.setFecha(LocalDateTime.now());
        BitacoraPagoDetalle bitPagoGuardado = bitacoraPagoDetalleRepo.save(bitPago);

        //Creación del detalle de la bitacora
        BitacoraDetallePagoDetalle detalle = new BitacoraDetallePagoDetalle();
        detalle.setBitacoraPagoDetalle(bitPagoGuardado);
        detalle.setUsuario(usuarioQueLoCrea);
        detalle.setDatosAnteriores(null);
        detalle.setDatosNuevos(jsonUtil.toJson(guardado));
        bitacoraDetalleRepo.save(detalle);

        return guardado;
    }

    @Transactional
    public Pago_Detalle actualizarConBitacora(Long id, Pago_Detalle cambios, String usuarioQueModifica) {

        // 1. Leemos el estado anterior
        Pago_Detalle anterior = pagoDetalleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe Pago_Detalle con id " + id));

        // Convertimos a JSON el objeto “anterior” (estado antes de la modificación)
        String datosAntes = jsonUtil.toJson(anterior);

        //Aplicación de cambios
        anterior.setConcepto(cambios.getConcepto());
        anterior.setDescripcion(cambios.getDescripcion());
        anterior.setMonto(cambios.getMonto());
        anterior.setServicioPagado(cambios.getServicioPagado());
        anterior.setEstadoPago(cambios.getEstadoPago());

        //Reserva reinstalación
        anterior.setReserva(cambios.getReserva());
        anterior.setReinstalacion(cambios.getReinstalacion());

        //Guardar el objeto modificado
        Pago_Detalle modificado = pagoDetalleRepo.save(anterior);

        //Registra en BitacoraPagoDetalle
        BitacoraPagoDetalle bitPago = new BitacoraPagoDetalle();
        bitPago.setPagoDetalle(modificado);
        bitPago.setOperacion("ACTUALIZACION");
        bitPago.setFecha(LocalDateTime.now());
        BitacoraPagoDetalle bitPagoGuardado = bitacoraPagoDetalleRepo.save(bitPago);

        //Creamos BitacoraDetallePagoDetalle con datosAnteriores y datosNuevos
        BitacoraDetallePagoDetalle detalle = new BitacoraDetallePagoDetalle();
        detalle.setBitacoraPagoDetalle(bitPagoGuardado);
        detalle.setUsuario(usuarioQueModifica);
        detalle.setDatosAnteriores(datosAntes);
        detalle.setDatosNuevos(jsonUtil.toJson(modificado));
        bitacoraDetalleRepo.save(detalle);

        return modificado;
    }

    @Transactional
    public void eliminarConBitacora(Long id, String usuarioQueElimina) {
        // 1. Leemos el estado antes de borrar
        Pago_Detalle anterior = pagoDetalleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe Pago_Detalle con id " + id));

        String datosAntes = jsonUtil.toJson(anterior);

        // 2. Registramos en BitacoraPagoDetalle (antes de borrar, para poder referenciar pagoDetalle)
        BitacoraPagoDetalle bitPago = new BitacoraPagoDetalle();
        bitPago.setPagoDetalle(anterior);
        bitPago.setOperacion("ELIMINACION");
        bitPago.setFecha(LocalDateTime.now());
        BitacoraPagoDetalle bitPagoGuardado = bitacoraPagoDetalleRepo.save(bitPago);

        // 3. Creamos BitacoraDetallePagoDetalle: datosAnteriores = JSON, datosNuevos = null
        BitacoraDetallePagoDetalle detalle = new BitacoraDetallePagoDetalle();
        detalle.setBitacoraPagoDetalle(bitPagoGuardado);
        detalle.setUsuario(usuarioQueElimina);
        detalle.setDatosAnteriores(datosAntes);
        detalle.setDatosNuevos(null);
        bitacoraDetalleRepo.save(detalle);

        // 4. Finalmente, eliminamos el Pago_Detalle
        pagoDetalleRepo.delete(anterior);
    }
}
