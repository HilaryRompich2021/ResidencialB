package com.example.Mi.casita.segura.reinstalacion.repository;

import com.example.Mi.casita.segura.reinstalacion.model.ReinstalacionServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReinstalacionRepository extends JpaRepository<ReinstalacionServicio, Long> {

    // Buscar reinstalaciones por usuario
    List<ReinstalacionServicio> findByUsuarioCui(String cui);

    // Buscar por estado (ej. SOLICITADO, COMPLETADO)
    List<ReinstalacionServicio> findByEstado(String estado);

    void deleteByUsuario_Cui(String cui);

}
