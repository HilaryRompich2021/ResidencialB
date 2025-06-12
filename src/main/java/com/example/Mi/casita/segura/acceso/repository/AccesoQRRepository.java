package com.example.Mi.casita.segura.acceso.repository;

import com.example.Mi.casita.segura.acceso.model.Acceso_QR;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccesoQRRepository extends JpaRepository<Acceso_QR, Long> {
    // Buscar por código QR exacto
    Optional<Acceso_QR> findByCodigoQR(String codigoQR);

    // Buscar todos los QR activos de un usuario (por CUI)
    List<Acceso_QR> findByAsociadoCuiAndEstado(String cui, String estado);

    // Buscar QR activos de visitantes (por ID visitante)
    Optional<Acceso_QR> findByVisitanteIdAndEstado(Long visitanteId, String estado);

    // Verificar si existe un QR vigente para un visitante
    boolean existsByVisitanteIdAndEstado(Long visitanteId, String estado);

    Optional<Acceso_QR> findFirstByAsociadoOrderByFechaGeneracionDesc(Usuario asociado);

    void deleteByAsociado_Cui(String cui);

}
