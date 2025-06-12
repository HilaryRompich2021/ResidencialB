package com.example.Mi.casita.segura.soporte.service;

import com.example.Mi.casita.segura.soporte.dto.CreateTicketRequestDTO;
import com.example.Mi.casita.segura.soporte.dto.TicketSoporteDTO;
import com.example.Mi.casita.segura.soporte.dto.UpdateEstadoRequestDTO;

import java.util.List;

public interface TicketSoporteService {

    TicketSoporteDTO crearTicket(CreateTicketRequestDTO request, String usuarioCuiLogeado);
    TicketSoporteDTO ponerEnProceso(UpdateEstadoRequestDTO request, String usuarioCuiLogeado);
    TicketSoporteDTO completarTicket(UpdateEstadoRequestDTO request, String usuarioCuiLogeado);
    //List<TicketSoporteDTO> listarTickets();
    TicketSoporteDTO obtenerPorId(Long id);
    List<TicketSoporteDTO> listarTickets(String usuarioCuiLogeado);
}
