package com.asbarbershop.usuario.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CrearUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres.")
    private String apellido;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres.")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato de correo no es válido.")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    @NotBlank(message = "El rol es obligatorio.")
    private String rol; // ADMIN, BARBERO, CLIENTE

    // Opcionales para barbero
    private String especialidad;

    @DecimalMin(value = "0.0", message = "La comisión no puede ser menor a 0.")
    @DecimalMax(value = "100.0", message = "La comisión no puede ser mayor a 100.")
    private BigDecimal porcentajeComision;

    // Opcionales para cliente
    private String preferencias;
}

