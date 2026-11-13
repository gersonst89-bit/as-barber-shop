package com.asbarbershop.comision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CambiarEstadoComisionRequest {

    @NotBlank(message = "El estado de la comisión es obligatorio.")
    @Pattern(regexp = "^(PENDIENTE|PAGADA|ANULADA)$", message = "El estado debe ser 'PENDIENTE', 'PAGADA' o 'ANULADA'.")
    private String nuevoEstado;
}

