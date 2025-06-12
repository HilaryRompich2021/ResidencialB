package com.example.Mi.casita.segura.usuarios.repository;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import jakarta.transaction.Transactional;
import jdk.jfr.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByCorreoElectronico(String correoElectronico);

    boolean existsByCorreoElectronico(String correoElectronico);

    // Buscar por nombre de usuario
    Optional<Usuario> findByUsuario(String usuario);

    // Verificar existencia por nombre de usuario
    boolean existsByUsuario(String usuario);

    // Buscar todos por rol
    List<Usuario> findAllByRol(String rol);

    //buscar por rol
    List<Usuario> findByRol(Usuario.Rol rol);

    // Para listar por roles (administradores + guardias por defecto)
    List<Usuario> findByRolIn(Collection<Usuario.Rol> roles);

    // Para búsqueda por nombre
    List<Usuario> findByNombreContainingIgnoreCase(
            String nombre
    );

    @Modifying
    @Query("UPDATE Usuario u SET u.estado = false WHERE u.cui = :cui")
    void desactivarUsuario(@Param("cui") String cui);

    List<Usuario> nombre(String nombre);
}
