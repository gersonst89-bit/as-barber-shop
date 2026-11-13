package com.asbarbershop.auth.controller;

import com.asbarbershop.auth.dto.AuthResponse;
import com.asbarbershop.auth.dto.LoginRequest;
import com.asbarbershop.auth.dto.RegistroClienteRequest;
import com.asbarbershop.auth.service.AuthService;
import com.asbarbershop.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Inicio de sesión exitoso.", response));
    }

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<AuthResponse>> registrarCliente(
            @Valid @RequestBody RegistroClienteRequest request) {
        AuthResponse response = authService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cliente registrado con éxito.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No autenticado."));
        }
        AuthResponse response = authService.obtenerUsuarioActual(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Información de usuario obtenida.", response));
    }
}

