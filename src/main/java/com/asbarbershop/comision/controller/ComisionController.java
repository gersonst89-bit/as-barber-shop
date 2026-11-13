package com.asbarbershop.comision.controller;

import com.asbarbershop.comision.dto.CambiarEstadoComisionRequest;
import com.asbarbershop.comision.dto.ComisionResponse;
import com.asbarbershop.comision.service.ComisionService;
import com.asbarbershop.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComisionController {

    private final ComisionService comisionService;

    @GetMapping("/comisiones")
    @PreAuthorize("hasAnyRole('ADMIN', 'BARBERO')")
    public ResponseEntity<ApiResponse<List<ComisionResponse>>> consultarComisiones(
            @RequestParam(required = false) Integer idBarbero,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<ComisionResponse> lista = comisionService.consultarComisiones(
                idBarbero, estado, fechaInicio, fechaFin, authentication.getName(), esAdmin);

        return ResponseEntity.ok(ApiResponse.success("Listado de comisiones obtenido.", lista));
    }

    @PatchMapping("/admin/comisiones/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComisionResponse>> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoComisionRequest request) {

        ComisionResponse response = comisionService.cambiarEstado(id, request.getNuevoEstado());
        return ResponseEntity.ok(ApiResponse.success("Estado de comisión actualizado exitosamente.", response));
    }
}

