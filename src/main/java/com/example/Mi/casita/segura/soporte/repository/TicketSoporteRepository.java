package com.example.Mi.casita.segura.soporte.repository;

import com.example.Mi.casita.segura.soporte.dto.TicketSoporteDTO;
import com.example.Mi.casita.segura.soporte.model.TicketSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long> {

    void deleteByUsuario_Cui(String cui);

    List<TicketSoporte> findByUsuario_Cui(String cui);

    //List<TicketSoporteDTO> listarTickets(String usuarioCuiLogeado);


}
