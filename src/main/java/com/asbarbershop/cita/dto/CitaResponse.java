package com.asbarbershop.cita.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaResponse {

    private Integer idCita;

    // Cliente
    private Integer idCliente;
    private String nombreCliente;
    private String telefonoCliente;
    private String emailCliente;

    // Barbero
    private Integer idBarbero;
    private String nombreBarbero;

    // Estado
    private Integer idEstado;
    private String estado;
    private String colorHex;

    // Horarios
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    // Textos
    private String notas;
    private String motivoCancelacion;

    // Montos
    private BigDecimal montoSubtotal;
    private BigDecimal montoDescuento;
    private BigDecimal montoTotal;

    // Promocion
    private String codigoPromocion;

    // Auditoria
    private String creadoPorNombre;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creadoEn;

    // Servicios
    private List<DetalleCitaResponse> servicios;
}

