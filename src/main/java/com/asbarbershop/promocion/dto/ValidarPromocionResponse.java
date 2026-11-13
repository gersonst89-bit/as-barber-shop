package com.asbarbershop.promocion.dto;

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
public class ValidarPromocionResponse {

    private boolean valida;
    private String mensaje;
    private Integer idPromocion;
    private String codigo;
    private String descripcion;
    private String tipoDescuento;
    private BigDecimal valorDescuento;
    private BigDecimal montoDescuentoCalculado;
    private BigDecimal montoFinal;
}

