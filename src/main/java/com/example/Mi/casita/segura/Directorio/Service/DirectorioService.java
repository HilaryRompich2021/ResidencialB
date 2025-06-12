package com.example.Mi.casita.segura.Directorio.Service;

import com.example.Mi.casita.segura.usuarios.dto.UsuarioListadoDTO;
import com.example.Mi.casita.segura.usuarios.model.Usuario;
import com.example.Mi.casita.segura.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DirectorioService {
    private final UsuarioRepository repo;

    public DirectorioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    /** Lista por defecto: administradores + guardias */
    public List<UsuarioListadoDTO> listaDefault() {
        List<Usuario> adminsYGuardias = repo.findByRolIn(
                Arrays.asList(Usuario.Rol.ADMINISTRADOR, Usuario.Rol.GUARDIA)
        );
        return mapToDto(adminsYGuardias);
    }

    /** Busca por nombre */
    public List<UsuarioListadoDTO> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listaDefault();
        }
        List<Usuario> encontrados = repo.findByNombreContainingIgnoreCase(texto);
        return mapToDto(encontrados);
    }

    private List<UsuarioListadoDTO> mapToDto(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(u -> new UsuarioListadoDTO(
                        u.getCui(),
                        u.getNombre(),
                        u.getCorreoElectronico(),
                        u.getTelefono(),
                        u.getNumeroCasa(),
                        u.getRol(),
                        u.isEstado()
                ))
                .collect(Collectors.toList());
    }

}
