package com.asbarbershop.cita.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialCitaResponse {

    private Integer idHistorial;
    private Integer idCita;

    private String estadoAnterior;
    private String estadoNuevo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAnterior;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicioAnterior;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFinAnterior;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNueva;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicioNueva;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFinNueva;

    private String barberoAnterior;
    private String barberoNuevo;
    private String usuarioCambio;
    private String observacion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCambio;
}

