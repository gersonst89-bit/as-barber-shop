package com.asbarbershop.cita.entity;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.cliente.entity.Cliente;
import com.asbarbershop.promocion.entity.Promocion;
import com.asbarbershop.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer idCita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_barbero", nullable = false)
    private Barbero barbero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoCita estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "notas", length = 255)
    private String notas;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @Column(name = "monto_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoSubtotal = BigDecimal.ZERO;

    @Column(name = "monto_descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
}