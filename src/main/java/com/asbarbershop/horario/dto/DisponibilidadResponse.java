package com.asbarbershop.horario.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadResponse {

    private Integer idBarbero;
    private String nombreBarbero;
    private LocalDate fecha;
    private String diaSemana;
    private Boolean tieneHorario;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horarioApertura;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horarioCierre;

    private List<LocalTime> horasDisponibles;
}

