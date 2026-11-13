package com.asbarbershop.cliente.controller;

import com.asbarbershop.cliente.dto.ClienteResponse;
import com.asbarbershop.cliente.service.ClienteService;
import com.asbarbershop.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping("/admin/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarClientes() {
        List<ClienteResponse> lista = clienteService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success("Listado de clientes obtenido.", lista));
    }

    @GetMapping("/admin/clientes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerClientePorId(@PathVariable Integer id) {
        ClienteResponse response = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Cliente encontrado.", response));
    }

    @GetMapping("/cliente/mi-perfil")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerMiPerfil(Authentication authentication) {
        ClienteResponse response = clienteService.obtenerPorEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Perfil del cliente obtenido.", response));
    }
}

