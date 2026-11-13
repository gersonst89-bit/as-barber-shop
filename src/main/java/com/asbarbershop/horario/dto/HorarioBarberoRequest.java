package com.asbarbershop.horario.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class HorarioBarberoRequest {

    @NotNull(message = "El día de la semana es obligatorio (1 = Lunes, 7 = Domingo).")
    @Min(value = 1, message = "El día de la semana debe ser entre 1 (Lunes) y 7 (Domingo).")
    @Max(value = 7, message = "El día de la semana debe ser entre 1 (Lunes) y 7 (Domingo).")
    private Byte diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private Boolean activo = true;
}

