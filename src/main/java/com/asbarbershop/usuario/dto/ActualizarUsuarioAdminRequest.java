package com.asbarbershop.usuario.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ActualizarUsuarioAdminRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres.")
    private String apellido;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres.")
    private String telefono;

    private String rol;

    private Boolean activo;

    // Campos barbero
    private String especialidad;

    @DecimalMin(value = "0.0", message = "La comisión no puede ser menor a 0.")
    @DecimalMax(value = "100.0", message = "La comisión no puede ser mayor a 100.")
    private BigDecimal porcentajeComision;

    private Boolean disponible;

    // Campos cliente
    private String preferencias;
}

