package com.example.Mi.casita.segura.soporte.Bitacora.repository;

import com.example.Mi.casita.segura.soporte.Bitacora.model.BitacoraTicketSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraTicketSoporteRepository extends JpaRepository<BitacoraTicketSoporte, Long> {

}
