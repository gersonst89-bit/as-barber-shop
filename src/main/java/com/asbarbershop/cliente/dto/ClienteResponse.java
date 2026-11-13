package com.asbarbershop.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {

    private Integer idCliente;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String preferencias;
    private LocalDate fechaRegistro;
    private Boolean activo;
}

