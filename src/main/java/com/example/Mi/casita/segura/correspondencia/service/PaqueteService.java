package com.example.Mi.casita.segura.correspondencia.service;


import com.example.Mi.casita.segura.Correo.Service.CorreoService;
import com.example.Mi.casita.segura.correspondencia.dto.CodigoDTO;
import com.example.Mi.casita.segura.correspondencia.dto.PaqueteRegistroDTO;
import com.example.Mi.casita.segura.correspondencia.dto.PaqueteResponseDTO;
import com.example.Mi.casita.segura.correspondencia.model.Paquete;
import com.example.Mi.casita.segura.correspondencia.repository.PaqueteRepository;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Service
public class PaqueteService {

    private final PaqueteRepository paqueteRepo;
    private final UsuarioRepository usuarioRepo;
    private final CorreoService correoService;

    public List<Paquete> obtenerPaquetesPorResidente(String cui) {
        // Asumiendo que tienes un método en el repositorio para filtrar por el CUI del usuario:
        return paqueteRepo.findByCreadopor_CuiOrderByFechaRegistroDesc(cui);
    }

    /**
     * Registra un nuevo paquete para el residente dado su CUI y DTO de registro.
     *
     * @param cuiResidente CUI del usuario autenticado (residente)
     * @param dto          Datos editables para registrar paquete
     * @return El Paquete recién creado
     */
    @Transactional
    public Paquete registrarPaquete(String cuiResidente, PaqueteRegistroDTO dto) {
        // 1) Validar que el residente exista y esté activo
        Usuario residente = usuarioRepo.findById(cuiResidente)
                .orElseThrow(() -> new IllegalArgumentException("Residente no encontrado"));
        if (!residente.isEstado()) {
            throw new IllegalArgumentException("Residente inactivo");
        }

        // 2) Crear nueva instancia de Paquete
        Paquete paquete = new Paquete();
        String codigo = UUID.randomUUID().toString();
        paquete.setCodigo(codigo);

        // 3) Copiar datos desde el DTO
        paquete.setEmpresaDeEntrega(dto.getEmpresaDeEntrega());
        paquete.setNumeroDeGuia(dto.getNumeroDeGuia());
        paquete.setTipoDePaquete(dto.getTipoDePaquete());
        paquete.setObservacion(dto.getObservacion());

        // 4) Poner fechas y estado inicial
        LocalDateTime ahora = LocalDateTime.now();
        paquete.setFechaRegistro(ahora);
        paquete.setFechaExpiracion(ahora.plusDays(7)); // válido durante 7 días
        paquete.setEstado(Paquete.EstadoPaquete.REGISTRADO);

        // 5) Relacionar con el residente que creó el paquete
        paquete.setCreadopor(residente);
        // 6) Guardar en BD y devolver
        Paquete paqueteGuardado = paqueteRepo.save(paquete);

        //Envío de correo
        correoService.enviarRegistroPaquete(
                residente.getCorreoElectronico(),
                residente.getNombre(),
                residente.getNumeroCasa(),
                paquete.getEmpresaDeEntrega(),
                paquete.getNumeroDeGuia(),
                paquete.getTipoDePaquete(),
                paquete.getObservacion(),
                paquete.getFechaRegistro(),
                paquete.getCodigo()
        );

        return paqueteRepo.save(paquete);
    }



    /**
     * Valida un código de llegada (códigoLlegada). Solo el guardia lo invoca.
     * Si el paquete existe, está en estado REGISTRADO y aún no expiró,
     * cambia estado a PENDIENTE_A_RECOGER y registra fechaRecepcion = ahora.
     */
    @Transactional
    public Paquete validarCodigoLlegada(CodigoDTO dto) {
        Paquete paquete = paqueteRepo.findByCodigo(dto.getCodigo())

                .orElseThrow(() -> new IllegalArgumentException("Código inválido"));

        if (paquete.getEstado() != Paquete.EstadoPaquete.REGISTRADO) {
            throw new IllegalArgumentException("El paquete no está en estado REGISTRADO");
        }
        if (LocalDateTime.now().isAfter(paquete.getFechaExpiracion())) {
            throw new IllegalArgumentException("El código ha expirado");
        }

        paquete.setFechaRecepcion(LocalDateTime.now());
        paquete.setEstado(Paquete.EstadoPaquete.PENDIENTE_A_RECOGER);

        Paquete actualizado = paqueteRepo.save(paquete);
        var residente = actualizado.getCreadopor();

        correoService.enviarLlegadaAGarita(
                residente.getCorreoElectronico(),
                residente.getNombre(),
                residente.getNumeroCasa(),
                paquete.getEmpresaDeEntrega(),
                paquete.getNumeroDeGuia(),
                paquete.getTipoDePaquete(),
                paquete.getObservacion(),
                paquete.getFechaRecepcion(),
                paquete.getCodigo()
        );
        return paqueteRepo.save(actualizado);


    }

    /**
     * Valida un código de entrega (códigoEntrega). Solo el guardia lo invoca.
     * Si el paquete existe, está en estado PENDIENTE_A_RECOGER y el código coincide,
     * cambia estado a ENTREGADO y registra fechaEntrega = ahora.
     */
    @Transactional
    public Paquete validarCodigoEntrega(CodigoDTO dto, String nombreGuardia) {
        Paquete paquete = paqueteRepo.findByCodigo(dto.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Código inválido"));

        if (paquete.getEstado() != Paquete.EstadoPaquete.PENDIENTE_A_RECOGER) {
            throw new IllegalArgumentException("El paquete no está en estado PENDIENTE_A_RECOGER");
        }
        // 1. Declaramos 'ahora' aquí:
        LocalDateTime ahora = LocalDateTime.now();

        paquete.setFechaEntrega(LocalDateTime.now());
        paquete.setEstado(Paquete.EstadoPaquete.ENTREGADO);
        Paquete entregado = paqueteRepo.save(paquete);

        // 2. Obtenemos datos del residente y guardia (supongamos que el nombre del guardia se pasa o se recupera)
        var residente = entregado.getCreadopor();
        //String nombreGuardia = /* obtén aquí el nombre o ID del guardia que hizo la entrega */;

        // 3. Enviamos correo al residente
        correoService.enviarEntregaAlResidente(
                residente.getCorreoElectronico(),
                residente.getNombre(),
                residente.getNumeroCasa(),
                entregado.getEmpresaDeEntrega(),
                entregado.getNumeroDeGuia(),
                entregado.getTipoDePaquete(),
                ahora,
                nombreGuardia
        );

        return paqueteRepo.save(entregado);
    }
    public PaqueteResponseDTO toDto(Paquete p) {
        PaqueteResponseDTO dto = new PaqueteResponseDTO();
        dto.setCodigo(p.getCodigo());
        dto.setEmpresaDeEntrega(p.getEmpresaDeEntrega());
        dto.setNumeroDeGuia(p.getNumeroDeGuia());
        dto.setTipoDePaquete(p.getTipoDePaquete());
        dto.setObservacion(p.getObservacion());
        dto.setFechaRegistro(p.getFechaRegistro());
        dto.setFechaExpiracion(p.getFechaExpiracion());
        dto.setFechaRecepcion(p.getFechaRecepcion());
        dto.setFechaEntrega(p.getFechaEntrega());
        dto.setEstado(p.getEstado().name());

        Usuario u = p.getCreadopor();
        dto.setCreadoPorCui(u.getCui());
        dto.setCreadoPorUsuario(u.getUsuario());
        dto.setCreadoPorNombre(u.getNombre());
        dto.setCreadoPorNumeroCasa(u.getNumeroCasa());
        return dto;
    }

    /*@Transactional
    public Paquete validarCodigoEntrega(CodigoDTO dto) {
        Optional<Paquete> opt = paqueteRepo.findByCodigoEntrega(dto.getCodigo());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Código de entrega inválido.");
        }

        Paquete paquete = opt.get();

        // Verificar estado actual
        if (paquete.getEstado() != Paquete.EstadoPaquete.PENDIENTE_A_RECOGER) {
            throw new IllegalArgumentException("El paquete no está pendiente de recogida.");
        }

        // Actualizar a Entregado
        paquete.setEstado(Paquete.EstadoPaquete.ENTREGADO);
        paquete.setFechaEntrega(LocalDateTime.now());

        return paqueteRepo.save(paquete);
    }*/
}
