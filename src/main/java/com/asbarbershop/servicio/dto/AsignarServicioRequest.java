package com.asbarbershop.servicio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AsignarServicioRequest {

    @NotNull(message = "El ID del barbero es obligatorio.")
    private Integer idBarbero;

    @NotNull(message = "El ID del servicio es obligatorio.")
    private Integer idServicio;
}

