// src/main/java/com/example/Mi/casita/segura/soporte/service/TicketSoporteServiceImpl.java
package com.example.Mi.casita.segura.soporte.service;

import com.example.Mi.casita.segura.pagos.Bitacora.CapturaDatos.JsonUtil;
import com.example.Mi.casita.segura.soporte.Bitacora.model.BitacoraDetalleTicketSoporte;
import com.example.Mi.casita.segura.soporte.Bitacora.model.BitacoraTicketSoporte;
import com.example.Mi.casita.segura.soporte.Bitacora.repository.BitacoraDetalleTicketSoporteRepository;
import com.example.Mi.casita.segura.soporte.Bitacora.repository.BitacoraTicketSoporteRepository;
import com.example.Mi.casita.segura.soporte.dto.CreateTicketRequestDTO;
import com.example.Mi.casita.segura.soporte.dto.TicketSoporteDTO;
import com.example.Mi.casita.segura.soporte.dto.UpdateEstadoRequestDTO;
import com.example.Mi.casita.segura.soporte.model.TicketSoporte;
//import com.example.Mi.casita.segura.soporte.model.Bitacora.BitacoraTicketSoporte;
//import com.example.Mi.casita.segura.soporte.model.Bitacora.BitacoraDetalleTicketSoporte;
import com.example.Mi.casita.segura.soporte.repository.TicketSoporteRepository;
//import com.example.Mi.casita.segura.soporte.repository.BitacoraTicketSoporteRepository;
//import com.example.Mi.casita.segura.soporte.repository.BitacoraDetalleTicketSoporteRepository;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketSoporteServiceImpl implements TicketSoporteService {

    private final TicketSoporteRepository ticketRepo;
    private final BitacoraTicketSoporteRepository bitacoraRepo;
    private final BitacoraDetalleTicketSoporteRepository detalleRepo;
    private final UsuarioRepository usuarioRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonUtil jsonUtil;

    @Override
    @Transactional
    public TicketSoporteDTO crearTicket(CreateTicketRequestDTO request, String usuarioLogeado) {
        Usuario usuario = usuarioRepo.findByUsuario(usuarioLogeado)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // 1) Crear y guardar el ticket
        TicketSoporte ticket = new TicketSoporte();
        ticket.setTipoError(request.getTipoError());
        ticket.setDescripcion(request.getDescripcion());
        ticket.setEstado("CREADO");
        ticket.setFechaCreacion(LocalDate.now());
        ticket.setFechaActualizacion(LocalDateTime.now());
        ticket.setUsuario(usuario);
        ticketRepo.save(ticket);

        // 2) Registrar cabecera de bitácora
        BitacoraTicketSoporte bitEnc = new BitacoraTicketSoporte();
        bitEnc.setTicketSoporte(ticket);
        bitEnc.setOperacion("CREACION");
        bitEnc.setFecha(LocalDateTime.now());
        bitacoraRepo.save(bitEnc);

        // 3) Serializar sólo el DTO plano (no la entidad entera)
        TicketSoporteDTO dtoAfter = mapToDTO(ticket);
        String snapshotDespues = jsonUtil.toJson(dtoAfter);

        // 4) Crear detalle de bitácora
        BitacoraDetalleTicketSoporte detalle = new BitacoraDetalleTicketSoporte();
        detalle.setBitacoraTicketSoporte(bitEnc);
        detalle.setUsuario(usuario.getNombre());
        detalle.setDatosAnteriores(null);
        detalle.setDatosNuevos(snapshotDespues);
        detalleRepo.save(detalle);

        return dtoAfter;
    }

    @Override
    @Transactional
    public TicketSoporteDTO ponerEnProceso(UpdateEstadoRequestDTO request, String usuarioLogeado) {
        // 1) Validar rol y cargar ticket
        Usuario usuario = usuarioRepo.findByUsuario(usuarioLogeado)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (!Usuario.Rol.ADMINISTRADOR.equals(usuario.getRol())) {
            throw new SecurityException("Solo administradores pueden poner en proceso");
        }
        TicketSoporte ticket = ticketRepo.findById(request.getTicketId())
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado"));

        // 2) Snapshot ANTERIOR usando DTO
        TicketSoporteDTO dtoBefore = mapToDTO(ticket);
        String snapshotAnterior = jsonUtil.toJson(dtoBefore);

        // 3) Actualizar y guardar
        ticket.setDescripcion(ticket.getDescripcion()
                + "\n\n--- DETALLE EN PROCESO ---\n"
                + request.getDetalle());
        ticket.setEstado("EN_PROCESO");
        ticket.setFechaActualizacion(LocalDateTime.now());
        ticket = ticketRepo.save(ticket);

        // 4) Cabecera bitácora
        BitacoraTicketSoporte bitEnc = new BitacoraTicketSoporte();
        bitEnc.setTicketSoporte(ticket);
        bitEnc.setOperacion("EN_PROCESO");
        bitEnc.setFecha(LocalDateTime.now());
        bitacoraRepo.save(bitEnc);

        // 5) Snapshot NUEVO usando DTO
        TicketSoporteDTO dtoAfter = mapToDTO(ticket);
        String snapshotNuevo = jsonUtil.toJson(dtoAfter);

        // 6) Detalle bitácora
        BitacoraDetalleTicketSoporte detalle = new BitacoraDetalleTicketSoporte();
        detalle.setBitacoraTicketSoporte(bitEnc);
        detalle.setUsuario(usuario.getNombre());
        detalle.setDatosAnteriores(snapshotAnterior);
        detalle.setDatosNuevos(snapshotNuevo);
        detalleRepo.save(detalle);

        return dtoAfter;
    }

    @Override
    @Transactional
    public TicketSoporteDTO completarTicket(UpdateEstadoRequestDTO request, String usuarioLogeado) {
        // 1) Validar rol y cargar ticket
        Usuario usuario = usuarioRepo.findByUsuario(usuarioLogeado)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        if (!Usuario.Rol.ADMINISTRADOR.equals(usuario.getRol())) {
            throw new SecurityException("Solo administradores pueden completar ticket");
        }
        TicketSoporte ticket = ticketRepo.findById(request.getTicketId())
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado"));

        // 2) Snapshot ANTERIOR usando DTO
        TicketSoporteDTO dtoBefore = mapToDTO(ticket);
        String snapshotAnterior = jsonUtil.toJson(dtoBefore);

        // 3) Actualizar y guardar
        ticket.setDescripcion(ticket.getDescripcion()
                + "\n\n--- DETALLE COMPLETADO ---\n"
                + request.getDetalle());
        ticket.setEstado("COMPLETADO");
        ticket.setFechaActualizacion(LocalDateTime.now());
        ticket = ticketRepo.save(ticket);

        // 4) Cabecera bitácora
        BitacoraTicketSoporte bitEnc = new BitacoraTicketSoporte();
        bitEnc.setTicketSoporte(ticket);
        bitEnc.setOperacion("COMPLETADO");
        bitEnc.setFecha(LocalDateTime.now());
        bitacoraRepo.save(bitEnc);

        // 5) Snapshot NUEVO usando DTO
        TicketSoporteDTO dtoAfter = mapToDTO(ticket);
        String snapshotNuevo = jsonUtil.toJson(dtoAfter);

        // 6) Detalle bitácora
        BitacoraDetalleTicketSoporte detalle = new BitacoraDetalleTicketSoporte();
        detalle.setBitacoraTicketSoporte(bitEnc);
        detalle.setUsuario(usuario.getNombre());
        detalle.setDatosAnteriores(snapshotAnterior);
        detalle.setDatosNuevos(snapshotNuevo);
        detalleRepo.save(detalle);

        return dtoAfter;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteDTO> listarTickets(String usuarioLogeado) {
        Usuario usuario = usuarioRepo.findByUsuario(usuarioLogeado)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        List<TicketSoporte> tickets;

        if (Usuario.Rol.ADMINISTRADOR.equals(usuario.getRol())) {
            tickets = ticketRepo.findAll();
        } else {
            tickets = ticketRepo.findByUsuario_Cui(usuario.getCui());
        }

        return tickets.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


/*
    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteDTO> listarTickets() {
        return ticketRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }*/

    @Override
    @Transactional(readOnly = true)
    public TicketSoporteDTO obtenerPorId(Long id) {
        TicketSoporte ticket = ticketRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket no encontrado"));
        return mapToDTO(ticket);
    }

    // -------------------------
    // Mapeo entidad → DTO
    private TicketSoporteDTO mapToDTO(TicketSoporte ticket) {
        TicketSoporteDTO dto = new TicketSoporteDTO();
        dto.setId(ticket.getId());
        dto.setTipoError(ticket.getTipoError());
        dto.setDescripcion(ticket.getDescripcion());
        dto.setEstado(ticket.getEstado());
        dto.setFechaCreacion(ticket.getFechaCreacion());
        dto.setFechaActualizacion(ticket.getFechaActualizacion());
        dto.setUsuarioCui(ticket.getUsuario().getCui());
        dto.setUsuarioNombre(ticket.getUsuario().getNombre());
        return dto;
    }
}
