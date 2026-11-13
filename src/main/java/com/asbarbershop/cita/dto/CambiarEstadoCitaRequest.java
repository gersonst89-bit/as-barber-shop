package com.asbarbershop.cita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CambiarEstadoCitaRequest {

    @NotBlank(message = "El nuevo estado es obligatorio.")
    private String nuevoEstado;

    @Size(max = 255, message = "La observación no puede exceder 255 caracteres.")
    private String observacion;
}

