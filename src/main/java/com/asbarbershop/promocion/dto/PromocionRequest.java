package com.asbarbershop.promocion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PromocionRequest {

    @NotBlank(message = "El código promocional es obligatorio.")
    @Size(max = 30, message = "El código no puede exceder 30 caracteres.")
    private String codigo;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres.")
    private String descripcion;

    @NotBlank(message = "El tipo de descuento es obligatorio (PORCENTAJE o MONTO_FIJO).")
    @Pattern(regexp = "^(PORCENTAJE|MONTO_FIJO)$", message = "El tipo de descuento debe ser 'PORCENTAJE' o 'MONTO_FIJO'.")
    private String tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio.")
    @DecimalMin(value = "0.01", message = "El valor del descuento debe ser mayor que 0.")
    private BigDecimal valorDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private Boolean activo = true;
}

