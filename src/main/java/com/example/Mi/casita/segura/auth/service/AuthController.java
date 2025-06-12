package com.example.Mi.casita.segura.auth.service;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins ={ "http://localhost:4200", "https://micasitaseguraresidencialf-cnema5azfjbxdje0.canadacentral-01.azurewebsites.net"})
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // 17) Usuario vacío
        if (request.getUsuario() == null || request.getUsuario().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(" El usuario no puede estar vacío, por favor ingresa tu usuario.");
        }

        // 19) Contraseña vacía
        if (request.getContrasena() == null || request.getContrasena().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(" La contraseña no puede estar vacía, por favor ingresa tu contraseña.");
        }

        // 20) Usuario no existe
        Optional<Usuario> opt = usuarioRepository.findByUsuario(request.getUsuario());
        if (opt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(" El usuario no existe.");
        }

        Usuario usuario = opt.get();

        // 18) Contraseña inválida
        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(" Contraseña inválida, intenta de nuevo.");
        }

        //   generar JWT
        String token = jwtService.generateToken(new UsuarioDetailsAdapter(usuario));
        return ResponseEntity.ok(new AuthResponse(token));
    }


}
