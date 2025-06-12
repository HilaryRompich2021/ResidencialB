// src/main/java/com/example/Mi/casita/segura/usuarios/service/UsuarioService.java
package com.example.Mi.casita.segura.usuarios.service;

import com.example.Mi.casita.segura.Correo.Service.CorreoService;
import com.example.Mi.casita.segura.Qr.service.QRService;
import com.example.Mi.casita.segura.acceso.model.Acceso_QR;
import com.example.Mi.casita.segura.acceso.repository.AccesoQRRepository;
import com.example.Mi.casita.segura.acceso.repository.RegistroIngresoRepository;
import com.example.Mi.casita.segura.correspondencia.repository.PaqueteRepository;
import com.example.Mi.casita.segura.notificaciones.service.NotificacionService;
import com.example.Mi.casita.segura.pagos.repository.PagoDetalleRepository;
import com.example.Mi.casita.segura.pagos.repository.PagosRepository;
import com.example.Mi.casita.segura.reinstalacion.repository.ReinstalacionRepository;
import com.example.Mi.casita.segura.reservas.repository.ReservaRepository;
import com.example.Mi.casita.segura.soporte.repository.TicketSoporteRepository;
import com.example.Mi.casita.segura.usuarios.dto.ActualizarPerfilDTO;
import com.example.Mi.casita.segura.usuarios.dto.UsuarioListadoDTO;
import com.example.Mi.casita.segura.usuarios.dto.UsuarioRegistroDTO;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import com.example.Mi.casita.segura.visitantes.repository.VisitanteRepository;
import com.google.zxing.WriterException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AccesoQRRepository accesoQRRepository;
    private final NotificacionService notificacionService;
    private final PasswordEncoder passwordEncoder;
    private final QRService qrService;
    private final CorreoService correoService;
    private final PagosRepository pagosRepository;
    private final PagoDetalleRepository pagoDetalleRepository;
    private final ReservaRepository reservaRepository;
    private final TicketSoporteRepository ticketSoporteRepository;
    private final ReinstalacionRepository reinstalacionRepository;
    private final VisitanteRepository visitanteRepository;
    private final PaqueteRepository paqueteRepository;
    private final RegistroIngresoRepository registroIngresoRepository;

    /**
     * Registra un usuario y genera un QR, devolviendo el DTO con el campo codigoQR rellenado.
     */
    public UsuarioRegistroDTO registrarUsuario(UsuarioRegistroDTO dto) {
        if (usuarioRepository.existsById(dto.getCui())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        // 2) Validar y ajustar número de casa según rol
        if (dto.getRol() == Usuario.Rol.ADMINISTRADOR || dto.getRol() == Usuario.Rol.GUARDIA) {
            dto.setNumeroCasa(0);
        } else {
            // RESIDENTE
            Integer nc = dto.getNumeroCasa();
            if (nc == null || nc < 1 || nc > 300) {
                throw new IllegalArgumentException("El número de casa debe estar entre 1 y 300 para residentes");
            }
        }

        // 3) Guardar la contraseña en texto plano en una variable local
        String contrasenaPlano = dto.getContrasena();

        // 1. Crear y guardar el Usuario
        Usuario usuario = new Usuario();
        usuario.setCui(dto.getCui());
        usuario.setNombre(dto.getNombre());
        usuario.setCorreoElectronico(dto.getCorreoElectronico());
        usuario.setUsuario(generarUsuarioUnico(dto.getNombre()));
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setRol(dto.getRol());
        usuario.setTelefono(dto.getTelefono());
        usuario.setFechaDeIngreso(LocalDate.now());
        usuario.setEstado(true);

        usuario.setNumeroCasa(dto.getNumeroCasa());


//guardar
        usuarioRepository.save(usuario);

        // 2. Generar y guardar el Acceso_QR para este usuario
        Acceso_QR qr = new Acceso_QR();
        qr.setCodigoQR(UUID.randomUUID().toString());
        LocalDateTime ahora = LocalDateTime.now();
        qr.setFechaGeneracion(ahora);
        qr.setFechaExpiracion(ahora.plusHours(24));  // expira en 24h
        qr.setEstado(Acceso_QR.Estado.ACTIVO);
        qr.setAsociado(usuario);
        accesoQRRepository.save(qr);



        // 5) Generar imagen del QR
        BufferedImage qrImage;
        try {
            qrImage = qrService.generarQR(qr.getCodigoQR());
        } catch (WriterException e) {
            // Si falla la generación del QR, lanzamos RuntimeException para revertir la transacción
            throw new RuntimeException("Error generando imagen del QR: " + e.getMessage(), e);
        }

        // 6) Enviar correo de bienvenida con credenciales y QR
        try {
            correoService.enviarBienvenida(
                    usuario.getCorreoElectronico(),
                    usuario.getNombre(),
                    usuario.getUsuario(),
                    contrasenaPlano,
                    qrImage
            );
        } catch (MessagingException | IOException e) {
            // Si falla el envío del correo, revertimos la transacción lanzando RuntimeException
            throw new RuntimeException("Error enviando correo de bienvenida: " + e.getMessage(), e);
        }

        return dto;
    }

    @Transactional
    public void eliminarUsuario(String cui) {
        if (usuarioRepository.existsById(cui)) {
            // borras transacciones
            pagosRepository.deleteByCreadoPor_Cui(cui);
            //pagoDetalleRepository.deleteByCreadoPorCui(cui);
            reservaRepository.deleteByResidente_Cui(cui);
            ticketSoporteRepository.deleteByUsuario_Cui(cui);
            reinstalacionRepository.deleteByUsuario_Cui(cui);
            visitanteRepository.deleteByCreadoPorCui(cui);
            paqueteRepository.deleteByCreadopor_Cui(cui);
            registroIngresoRepository.deleteByUsuario_Cui(cui);
            accesoQRRepository.deleteByAsociado_Cui(cui);


            // …las demás
            // luego borras el usuario
            usuarioRepository.deleteById(cui);
        } else {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
    }


    public void actualizarUsuario(UsuarioRegistroDTO dto) {
        // implementar según necesidades...
    }

    public List<UsuarioListadoDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> {
                    // Busca el último QR generado
                    String qrCode = accesoQRRepository
                            .findFirstByAsociadoOrderByFechaGeneracionDesc(u)
                            .map(Acceso_QR::getCodigoQR)
                            .orElse(null);  // o "" si prefieres cadena vacía

                    // Construye el DTO incluyendo el QR
                    return new UsuarioListadoDTO(
                            u.getCui(),
                            u.getNombre(),
                            u.getCorreoElectronico(),
                            u.getTelefono(),
                            u.getNumeroCasa(),
                            u.getRol(),
                            u.isEstado()
                    );
                })
                .collect(Collectors.toList());
    }

    /** Lógica para generar un nombre de usuario único a partir del nombre completo */
    public String generarUsuarioUnico(String nombreCompleto) {
        String[] palabras = nombreCompleto.trim().split("\\s+");
        if (palabras.length < 2) {
            throw new IllegalArgumentException("Debe ingresar al menos dos nombres");
        }
        String primera = palabras[0].toLowerCase();
        String ultima  = palabras[palabras.length - 1].toLowerCase();

        // Intentos con prefijo creciente
        for (int i = 1; i <= primera.length(); i++) {
            String intento = primera.substring(0, i) + ultima;
            if (!usuarioRepository.existsByUsuario(intento)) {
                return intento;
            }
        }
        // Si ya no queda, agregamos número
        String base = primera + (palabras.length > 1 ? palabras[1].toLowerCase() : "");
        int contador = 1;
        String intentoFinal;
        do {
            intentoFinal = base + contador++;
        } while (usuarioRepository.existsByUsuario(intentoFinal));
        return intentoFinal;
    }


    /**
     * Retorna todos los usuarios cuyo nombre contenga q.
     * Si q es null o vacío, devuelve todos.
     */
    public List<UsuarioListadoDTO> buscarDirectorio(String q) {
        List<Usuario> usuarios = (q == null || q.isBlank())
                ? usuarioRepository.findAll()
                : usuarioRepository.findByNombreContainingIgnoreCase(q);

        return usuarios.stream()
                .map(u -> {
                    // Opcionalmente incluyes también el código QR aquí
                    String qrCode = accesoQRRepository
                            .findFirstByAsociadoOrderByFechaGeneracionDesc(u)
                            .map(Acceso_QR::getCodigoQR)
                            .orElse(null);

                    return new UsuarioListadoDTO(
                            u.getCui(),
                            u.getNombre(),
                            u.getCorreoElectronico(),
                            u.getTelefono(),
                            u.getNumeroCasa(),
                            u.getRol(),
                            u.isEstado()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Usuario findByUsernameOrThrow(String username) {
        return usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Obtiene un DTO con los datos del usuario para mostrar en el perfil.
     */
    @Transactional(readOnly = true)
    public UsuarioListadoDTO obtenerPerfil(String username) {
        Usuario u = findByUsernameOrThrow(username);
        return new UsuarioListadoDTO(
                u.getCui(),
                u.getNombre(),
                u.getCorreoElectronico(),
                u.getTelefono(),
                u.getNumeroCasa(),
                u.getRol(),
                u.isEstado()
        );
    }

    /**
     * Actualiza únicamente correoElectronico y teléfono del usuario identificado por 'username'.
     */
    public UsuarioListadoDTO actualizarPerfil(String username, ActualizarPerfilDTO dto) {
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setCorreoElectronico(dto.getCorreoElectronico());
        usuario.setTelefono(dto.getTelefono());
        usuarioRepository.save(usuario);

        return new UsuarioListadoDTO(
                usuario.getCui(),
                usuario.getNombre(),
                usuario.getCorreoElectronico(),
                usuario.getTelefono(),
                usuario.getNumeroCasa(),
                usuario.getRol(),
                usuario.isEstado()
        );
    }

}
