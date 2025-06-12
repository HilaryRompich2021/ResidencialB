package com.example.Mi.casita.segura.pagos.controller;

import com.example.Mi.casita.segura.pagos.dto.AguaCargoDTO;
import com.example.Mi.casita.segura.pagos.dto.PagoConsultaDTO;
import com.example.Mi.casita.segura.pagos.dto.PagoRequestDTO;
import com.example.Mi.casita.segura.pagos.model.Pagos;
import com.example.Mi.casita.segura.pagos.repository.PagosRepository;
import com.example.Mi.casita.segura.pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//@Controller
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final PagosRepository pagosRepo;
    //private final Pagos pagos;

    @PostMapping("/registrarPago")
    @PreAuthorize("hasRole('RESIDENTE') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> registrar(@Valid @RequestBody PagoRequestDTO dto) {
        try {
            pagoService.registrarPago(dto);
            return ResponseEntity.ok(Map.of("mensaje", "Pago registrado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/pendientes/{cui}")
    public ResponseEntity<List<Pagos>> obtenerPagosPendientes(@PathVariable String cui) {
        List<Pagos> pagos = pagosRepo.findByCreadoPor_CuiAndEstado(cui, Pagos.EstadoDelPago.PENDIENTE);
        return ResponseEntity.ok(pagos);
    }

    @PreAuthorize("hasRole('RESIDENTE') or hasRole('ADMINISTRADOR')")
    @GetMapping("/todos/{cui}")
    public List<Pagos> obtenerTodosLosPagos(@PathVariable String cui) {
        //return pagosRepo.findByCreadoPor_Cui(cui);
        return pagoService.obtenerPagosPorUsuario(cui);
    }

    @GetMapping(value ="/listar/{cui}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PagoConsultaDTO>> listarPagos(@PathVariable String cui) {
        List<PagoConsultaDTO> pagos = pagoService.obtenerPagosPorCui(cui);
        return ResponseEntity.ok(pagos);
    }

    @PostMapping("/cargo-agua")
    public ResponseEntity<?> cargoAgua(@RequestBody AguaCargoDTO dto) {
        // Puedes validar aquí que el usuario actual tenga rol ADMIN antes de procesar
        Pagos pago = pagoService.generarCargoAgua(dto);
        return ResponseEntity.ok(pago);
    }

}
