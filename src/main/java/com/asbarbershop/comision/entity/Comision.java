package com.asbarbershop.comision.entity;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.cita.entity.Cita;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comisiones")
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comision")
    private Integer idComision;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false, unique = true)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_barbero", nullable = false)
    private Barbero barbero;

    @Column(name = "monto_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoBase;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "monto_comision", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoComision;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}