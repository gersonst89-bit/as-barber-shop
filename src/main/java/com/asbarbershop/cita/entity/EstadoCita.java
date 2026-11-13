package com.asbarbershop.cita.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "estados_cita")
public class EstadoCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer idEstado;

    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;
}