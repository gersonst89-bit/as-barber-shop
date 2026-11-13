package com.asbarbershop.servicio.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ServicioRequest {

    @NotBlank(message = "El nombre del servicio es obligatorio.")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo.")
    private BigDecimal precio;

    @NotNull(message = "La duración en minutos es obligatoria.")
    @Min(value = 1, message = "La duración mínima es de 1 minuto.")
    private Integer duracionMinutos;

    private Boolean activo = true;
}

