package com.asbarbershop.pago.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {

    private Integer idPago;
    private Integer idCita;
    private String nombreCliente;
    private String nombreBarbero;
    private Integer idMetodo;
    private String metodoPago;
    private BigDecimal monto;
    private String referencia;
    private String estadoPago;
    private String registradoPor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaPago;
}

