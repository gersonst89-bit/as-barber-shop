package com.asbarbershop.pago.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "metodos_pago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo")
    private Integer idMetodo;

    @Column(name = "nombre", nullable = false, unique = true, length = 40)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}