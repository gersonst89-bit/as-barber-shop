package com.asbarbershop.barbero.controller;

import com.asbarbershop.barbero.dto.ActualizarBarberoRequest;
import com.asbarbershop.barbero.dto.BarberoResponse;
import com.asbarbershop.barbero.service.BarberoService;
import com.asbarbershop.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/barberos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBarberoController {

    private final BarberoService barberoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BarberoResponse>>> listarTodos() {
        List<BarberoResponse> lista = barberoService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success("Listado general de barberos obtenido.", lista));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BarberoResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarBarberoRequest request) {
        BarberoResponse response = barberoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Datos del barbero actualizados correctamente.", response));
    }
}

