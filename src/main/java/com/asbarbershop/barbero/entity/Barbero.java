package com.asbarbershop.barbero.entity;

import com.asbarbershop.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "barberos")
public class Barbero {

    @Id
    @Column(name = "id_barbero")
    private Integer idBarbero;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_barbero", nullable = false)
    private Usuario usuario;

    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Column(
            name = "porcentaje_comision",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal porcentajeComision = BigDecimal.ZERO;

    @Column(name = "disponible", nullable = false)
    private Boolean disponible = true;
}