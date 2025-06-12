package com.example.Mi.casita.segura.visitantes.controller;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import com.example.Mi.casita.segura.visitantes.dto.VisitanteEstadoDTO;
import com.example.Mi.casita.segura.visitantes.dto.VisitanteListadoDTO;
import com.example.Mi.casita.segura.visitantes.dto.VisitanteRegistroDTO;
import com.example.Mi.casita.segura.visitantes.model.Visitante;
import com.example.Mi.casita.segura.visitantes.service.VisitanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/visitantes")
@RequiredArgsConstructor
public class VisitanteController {

    private final VisitanteService visitanteService;
    private final UsuarioRepository usuarioRepo;

    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('RESIDENTE')")
    @PostMapping("/registro")
    public ResponseEntity<Visitante> registrarVisitante(
            @Valid @RequestBody VisitanteRegistroDTO dto
    ) {
        Visitante v = visitanteService.registrarVisitante(dto);
        return ResponseEntity.ok(v);
    }

    @GetMapping
    public ResponseEntity<List<VisitanteListadoDTO>> listarVisitantes() {
        return ResponseEntity.ok(visitanteService.obtenerTodosVisitantes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visitante> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody VisitanteRegistroDTO dto) {
        Visitante actualizado = visitanteService.actualizarVisitante(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/propios")
    public List<VisitanteListadoDTO> obtenerPropios(Authentication auth) {
        // 1) auth.getName() devuelve el nombre de usuario (sub) de tu JWT
        String nombreUsuario = auth.getName();
        // 2) busco el Usuario para extraer su CUI (o, si guardaste el CUI como sub en el token, omites este paso)
        Usuario u = usuarioRepo.findByUsuario(nombreUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        // 3) devuelvo lista filtrada
        return visitanteService.listaDefault(u.getCui());
    }

    //@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('RESIDENTE')")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<VisitanteListadoDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody VisitanteEstadoDTO estadoDTO
    ) {
        Visitante actualizado = visitanteService.cambiarEstado(id, estadoDTO.getEstado());
        VisitanteListadoDTO dto = visitanteService.toListadoDTO(actualizado);
        return ResponseEntity.ok(dto);
    }



}
