package com.example.Mi.casita.segura.reservas.controller;

import com.example.Mi.casita.segura.reservas.dto.ReservaDTO;
import com.example.Mi.casita.segura.reservas.dto.ReservaListadoDTO;
import com.example.Mi.casita.segura.reservas.service.ReservaService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearReserva(@RequestBody ReservaDTO dto) {
        reservaService.crearReserva(dto); // ← aquí truena si está en null
        return ResponseEntity.ok("OK");
    }

    /** Listar reservas activas de un residente (propias) */
    @GetMapping("/propias/{cui}")
    public ResponseEntity<List<ReservaListadoDTO>> listarReservasPropias(
            @PathVariable("cui") String cui) {
        List<ReservaListadoDTO> lista = reservaService.obtenerReservasActivasPorCui(cui);
        return ResponseEntity.ok(lista);
    }

    /** ADMIN → solo reservas confirmadas (“RESERVADO”), ordenadas por fecha/hora */
    @GetMapping("/todas-confirmadas")
    public ResponseEntity<List<ReservaListadoDTO>> listarReservasConfirmadas() {
        List<ReservaListadoDTO> lista = reservaService.obtenerReservasConfirmadasOrdenadas();
        return ResponseEntity.ok(lista);
    }

}
