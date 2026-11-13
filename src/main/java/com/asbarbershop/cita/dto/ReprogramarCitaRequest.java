package com.asbarbershop.cita.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ReprogramarCitaRequest {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora de inicio es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime nuevaHoraInicio;

    // Opcional: permite cambiar también de barbero al reprogramar
    private Integer nuevoIdBarbero;

    @Size(max = 255, message = "La observación no puede exceder 255 caracteres.")
    private String observacion;
}

