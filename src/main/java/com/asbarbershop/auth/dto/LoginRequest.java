package com.asbarbershop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato del correo electrónico no es válido.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String password;
}

