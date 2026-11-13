package com.asbarbershop.usuario.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.usuario.dto.ActualizarPerfilRequest;
import com.asbarbershop.usuario.dto.CambiarPasswordRequest;
import com.asbarbershop.usuario.dto.UsuarioResponse;
import com.asbarbershop.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPerfil(Authentication authentication) {
        UsuarioResponse response = usuarioService.obtenerPorEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Perfil de usuario obtenido.", response));
    }

    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        UsuarioResponse response = usuarioService.actualizarPerfil(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado con éxito.", response));
    }

    @PutMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            Authentication authentication,
            @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada exitosamente.", null));
    }
}

