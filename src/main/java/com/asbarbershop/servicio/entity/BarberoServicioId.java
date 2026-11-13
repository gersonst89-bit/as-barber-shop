package com.asbarbershop.servicio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class BarberoServicioId implements Serializable {

    @Column(name = "id_barbero")
    private Integer idBarbero;

    @Column(name = "id_servicio")
    private Integer idServicio;
}