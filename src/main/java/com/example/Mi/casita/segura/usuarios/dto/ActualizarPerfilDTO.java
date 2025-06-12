package com.example.Mi.casita.segura.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ActualizarPerfilDTO {
    @NotBlank(message = "El correo no puede quedar vacío")
    @Email(message = "Formato de correo inválido")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|org|net|edu|gov|mil|gt)$",
            message = "El correo debe ser una dirección válida (ej.: usuario@dominio.com)"
    )
    private String correoElectronico;

    @NotBlank(message = "El teléfono no puede quedar vacío")
    @Pattern(regexp = "\\d{8}", message = "El teléfono debe tener exactamente 8 dígitos")
    private String telefono;
}
