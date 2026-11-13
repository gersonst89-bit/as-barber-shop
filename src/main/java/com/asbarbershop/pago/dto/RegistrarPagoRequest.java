package com.asbarbershop.pago.dto;

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
public class RegistrarPagoRequest {

    @NotNull(message = "El ID de la cita es obligatorio.")
    private Integer idCita;

    @NotNull(message = "El ID del método de pago es obligatorio.")
    private Integer idMetodo;

    @NotNull(message = "El monto del pago es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto a pagar debe ser mayor que cero.")
    private BigDecimal monto;

    @Size(max = 80, message = "La referencia no puede exceder 80 caracteres.")
    private String referencia;
}

