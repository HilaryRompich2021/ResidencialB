package com.example.Mi.casita.segura.usuarios.dto;

import com.example.Mi.casita.segura.usuarios.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioListadoDTO {
    private String cui;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private int numeroCasa;
    private Usuario.Rol rol;
    private boolean estado;
}
