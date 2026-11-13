package com.asbarbershop.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres.")
    private String apellido;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres.")
    private String telefono;

    // Solo si el usuario es cliente
    private String preferencias;
}

