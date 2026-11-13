package com.asbarbershop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistroClienteRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres.")
    private String apellido;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres.")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato del correo electrónico no es válido.")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    @Size(max = 255, message = "Las preferencias no pueden exceder 255 caracteres.")
    private String preferencias;
}

