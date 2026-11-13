package com.asbarbershop.horario.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioBarberoResponse {

    private Integer idHorario;
    private Integer idBarbero;
    private String nombreBarbero;
    private Byte diaSemana;
    private String nombreDiaSemana;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private Boolean activo;
}

