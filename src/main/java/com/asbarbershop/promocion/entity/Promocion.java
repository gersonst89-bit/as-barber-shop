package com.asbarbershop.promocion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promociones")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Integer idPromocion;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "tipo_descuento", nullable = false, length = 20)
    private String tipoDescuento;

    @Column(name = "valor_descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDescuento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}