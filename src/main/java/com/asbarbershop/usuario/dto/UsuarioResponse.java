package com.asbarbershop.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String rol;
    private Boolean activo;
    private LocalDateTime creadoEn;

    // Campos si es cliente
    private String preferencias;
    private LocalDate fechaRegistro;

    // Campos si es barbero
    private String especialidad;
    private BigDecimal porcentajeComision;
    private Boolean disponible;
}

