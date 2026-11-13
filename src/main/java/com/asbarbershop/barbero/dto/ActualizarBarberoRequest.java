package com.asbarbershop.barbero.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ActualizarBarberoRequest {

    @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres.")
    private String especialidad;

    @NotNull(message = "El porcentaje de comisión es obligatorio.")
    @DecimalMin(value = "0.0", message = "La comisión no puede ser menor a 0.")
    @DecimalMax(value = "100.0", message = "La comisión no puede ser mayor a 100.")
    private BigDecimal porcentajeComision;

    @NotNull(message = "El estado disponible es obligatorio.")
    private Boolean disponible;
}

