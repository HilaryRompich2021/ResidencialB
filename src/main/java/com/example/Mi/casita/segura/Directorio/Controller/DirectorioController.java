package com.example.Mi.casita.segura.Directorio.Controller;

import com.example.Mi.casita.segura.Directorio.Service.DirectorioService;
import com.example.Mi.casita.segura.usuarios.dto.UsuarioListadoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/directorio")
public class DirectorioController {
    private final DirectorioService directorioService;

    public DirectorioController(DirectorioService directorioService) {
        this.directorioService = directorioService;
    }

    /** GET sin parámetro → lista por defecto */
    @GetMapping
    public List<UsuarioListadoDTO> obtenerDirectorio(
            @RequestParam(value="q", required=false)
            String q
    ) {
        return (q == null)
                ? directorioService.listaDefault()
                : directorioService.buscar(q);
    }
}
