package com.asbarbershop.pago.entity;

import com.asbarbershop.cita.entity.Cita;
import com.asbarbershop.usuario.entity.Usuario;
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
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cita", nullable = false)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo", nullable = false)
    private MetodoPago metodoPago;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "referencia", length = 80)
    private String referencia;

    @Column(name = "estado_pago", nullable = false, length = 20)
    private String estadoPago = "CONFIRMADO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @CreationTimestamp
    @Column(name = "fecha_pago", nullable = false, updatable = false)
    private LocalDateTime fechaPago;
}