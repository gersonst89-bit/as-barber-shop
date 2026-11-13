package com.asbarbershop.cliente.entity;

import com.asbarbershop.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @Column(name = "id_cliente")
    private Integer idCliente;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario usuario;

    @Column(name = "preferencias", length = 255)
    private String preferencias;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;
}