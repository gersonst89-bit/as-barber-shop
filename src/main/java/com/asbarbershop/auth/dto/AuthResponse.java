package com.asbarbershop.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    @Builder.Default
    private String tipoToken = "Bearer";
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private Integer idPerfil; // idCliente si es cliente, idBarbero si es barbero
}

