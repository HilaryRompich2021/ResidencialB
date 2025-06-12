package com.example.Mi.casita.segura.acceso.repository;

import com.example.Mi.casita.segura.acceso.model.Bitacora.BitacoraDetalleRegistroIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraDetalleRegistroIngresoRepository
        extends JpaRepository<BitacoraDetalleRegistroIngreso, Long> {
}