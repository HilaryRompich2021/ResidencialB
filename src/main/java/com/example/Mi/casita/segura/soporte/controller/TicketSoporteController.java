package com.example.Mi.casita.segura.soporte.controller;

import com.example.Mi.casita.segura.soporte.dto.CreateTicketRequestDTO;
import com.example.Mi.casita.segura.soporte.dto.TicketSoporteDTO;
import com.example.Mi.casita.segura.soporte.dto.UpdateEstadoRequestDTO;
import com.example.Mi.casita.segura.soporte.service.TicketSoporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Validated
public class TicketSoporteController {

    private final TicketSoporteService ticketService;

    /**
     * POST /api/tickets
     * Crea un ticket nuevo.
     * Cualquier usuario autenticado (RESIDENTE, GUARDIA, ADMINISTRADOR) puede llamar.
     */
    @PostMapping
    public ResponseEntity<TicketSoporteDTO> crearTicket(
            @Valid @RequestBody CreateTicketRequestDTO request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        // principal.getUsername() debe ser el CUI (13 dígitos) del usuario logeado
        String cuiLogeado = principal.getUsername();
        TicketSoporteDTO created = ticketService.crearTicket(request, cuiLogeado);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/tickets/en-proceso
     * Solo ADMINISTRADOR puede llamar a este endpoint para poner en proceso.
     */
    @PutMapping("/en-proceso")
    public ResponseEntity<TicketSoporteDTO> ponerEnProceso(
            @Valid @RequestBody UpdateEstadoRequestDTO request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String cuiLogeado = principal.getUsername();
        TicketSoporteDTO updated = ticketService.ponerEnProceso(request, cuiLogeado);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/tickets/completar
     * Solo ADMINISTRADOR puede llamar a este endpoint para marcar completado.
     */
    @PutMapping("/completar")
    public ResponseEntity<TicketSoporteDTO> completarTicket(
            @Valid @RequestBody UpdateEstadoRequestDTO request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String cuiLogeado = principal.getUsername();
        TicketSoporteDTO updated = ticketService.completarTicket(request, cuiLogeado);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/tickets
     * Lista todos los tickets.
     */
    @GetMapping
    public ResponseEntity<List<TicketSoporteDTO>> listarTickets(@AuthenticationPrincipal UserDetails principal) {
        String cuiLogeado = principal.getUsername();
        List<TicketSoporteDTO> list = ticketService.listarTickets(cuiLogeado);
        return ResponseEntity.ok(list);
    }

   /* @GetMapping
    public ResponseEntity<List<TicketSoporteDTO>> listarTickets() {
        List<TicketSoporteDTO> list = ticketService.listarTickets();
        return ResponseEntity.ok(list);
    }*/

    /**
     * GET /api/tickets/{id}
     * Obtiene un ticket por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketSoporteDTO> obtenerPorId(@PathVariable Long id) {
        TicketSoporteDTO dto = ticketService.obtenerPorId(id);
        return ResponseEntity.ok(dto);
    }
}
