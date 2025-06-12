package com.example.Mi.casita.segura.correspondencia.repository;

import com.example.Mi.casita.segura.correspondencia.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaqueteRepository extends JpaRepository<Paquete, String> {
    //Optional<Paquete> findByCodigoLlegada(String codigoLlegada);
    Optional<Paquete> findByCodigo(String codigo);
    List<Paquete> findByCreadopor_CuiOrderByFechaRegistroDesc(String cui);
   // Optional<Paquete> findByCodigoEntrega(String codigoEntrega);

    void deleteByCreadopor_Cui(String cui);


}

