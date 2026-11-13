package com.asbarbershop.cita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CancelarCitaRequest {

    @NotBlank(message = "El motivo de cancelación es obligatorio.")
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres.")
    private String motivoCancelacion;
}

