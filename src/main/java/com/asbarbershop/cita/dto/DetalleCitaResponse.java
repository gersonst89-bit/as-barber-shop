package com.asbarbershop.cita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCitaResponse {

    private Integer idDetalle;
    private Integer idServicio;
    private String nombreServicio;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private Integer duracionMinutos;
}

