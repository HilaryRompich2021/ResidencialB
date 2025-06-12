package com.example.Mi.casita.segura.visitantes.repository;

import com.example.Mi.casita.segura.visitantes.model.Visitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Long> {
    boolean existsByCui(String cui);

    List<Visitante> findByCreadoPor_Cui(String creadorCui);

    void deleteByCreadoPorCui(String cui);

}