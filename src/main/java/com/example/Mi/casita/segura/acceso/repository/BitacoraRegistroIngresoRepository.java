package com.example.Mi.casita.segura.acceso.repository;

import com.example.Mi.casita.segura.acceso.model.Bitacora.BitacoraRegistroIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraRegistroIngresoRepository
        extends JpaRepository<BitacoraRegistroIngreso, Long> {
}