package com.asbarbershop.comision.dto;

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
public class ComisionResponse {

    private Integer idComision;
    private Integer idCita;
    private Integer idBarbero;
    private String nombreBarbero;
    private BigDecimal montoBase;
    private BigDecimal porcentaje;
    private BigDecimal montoComision;
    private String estado;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creadoEn;
}

