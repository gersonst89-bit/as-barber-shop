package com.asbarbershop.cita.entity;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "historial_citas")
public class HistorialCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_anterior")
    private EstadoCita estadoAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_nuevo", nullable = false)
    private EstadoCita estadoNuevo;

    @Column(name = "fecha_anterior")
    private LocalDate fechaAnterior;

    @Column(name = "hora_inicio_anterior")
    private LocalTime horaInicioAnterior;

    @Column(name = "hora_fin_anterior")
    private LocalTime horaFinAnterior;

    @Column(name = "fecha_nueva")
    private LocalDate fechaNueva;

    @Column(name = "hora_inicio_nueva")
    private LocalTime horaInicioNueva;

    @Column(name = "hora_fin_nueva")
    private LocalTime horaFinNueva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_barbero_anterior")
    private Barbero barberoAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_barbero_nuevo")
    private Barbero barberoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cambio", nullable = false)
    private Usuario usuarioCambio;

    @Column(name = "observacion", length = 255)
    private String observacion;

    @CreationTimestamp
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;
}