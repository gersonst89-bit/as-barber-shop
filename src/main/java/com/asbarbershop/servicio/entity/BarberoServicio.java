package com.asbarbershop.servicio.entity;

import com.asbarbershop.barbero.entity.Barbero;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "barbero_servicio")
public class BarberoServicio {

    @EmbeddedId
    private BarberoServicioId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idBarbero")
    @JoinColumn(name = "id_barbero", nullable = false)
    private Barbero barbero;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idServicio")
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;
}