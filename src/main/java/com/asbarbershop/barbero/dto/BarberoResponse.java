package com.asbarbershop.barbero.dto;

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
public class BarberoResponse {

    private Integer idBarbero;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String especialidad;
    private BigDecimal porcentajeComision;
    private Boolean disponible;
    private Boolean activo;
}

