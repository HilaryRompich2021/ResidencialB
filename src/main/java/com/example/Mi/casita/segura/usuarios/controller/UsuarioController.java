package com.example.Mi.casita.segura.usuarios.controller;

import com.example.Mi.casita.segura.usuarios.dto.ActualizarPerfilDTO;
import com.example.Mi.casita.segura.usuarios.dto.UsuarioListadoDTO;
import com.example.Mi.casita.segura.usuarios.dto.UsuarioRegistroDTO;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioRegistroDTO> registrar(
            @Valid @RequestBody UsuarioRegistroDTO dto) {
        // Ahora devolvemos el DTO con el campo codigoQR ya rellenado
        UsuarioRegistroDTO respuesta = usuarioService.registrarUsuario(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }


    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{cui}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable String cui) {
        usuarioService.eliminarUsuario(cui);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @GetMapping
    public ResponseEntity<List<UsuarioListadoDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }

    @GetMapping("/directorio")
    public ResponseEntity<List<UsuarioListadoDTO>> directorio(
            @RequestParam(name = "q", required = false) String q) {
        List<UsuarioListadoDTO> lista = usuarioService.buscarDirectorio(q);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioListadoDTO> perfilActual(Authentication auth) {
        // auth.getName() es el "username" (campo 'usuario') dentro del JWT
        String username = auth.getName();
        UsuarioListadoDTO dto = usuarioService.obtenerPerfil(username);
        return ResponseEntity.ok(dto);
    }


    @PutMapping("/me")
    public ResponseEntity<UsuarioListadoDTO> actualizarMiPerfil(
            Authentication auth,
            @Valid @RequestBody ActualizarPerfilDTO dto
    ) {
        String username = auth.getName();
        UsuarioListadoDTO actualizado = usuarioService.actualizarPerfil(username, dto);
        return ResponseEntity.ok(actualizado);
    }
}
