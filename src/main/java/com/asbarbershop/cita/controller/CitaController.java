package com.asbarbershop.cita.controller;

import com.asbarbershop.cita.dto.*;
import com.asbarbershop.cita.service.CitaService;
import com.asbarbershop.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @PostMapping
    public ResponseEntity<ApiResponse<CitaResponse>> crearCita(
            @Valid @RequestBody CrearCitaRequest request,
            Authentication authentication) {

        CitaResponse response = citaService.crearCita(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cita reservada exitosamente.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CitaResponse>>> consultarCitas(
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) Integer idBarbero,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean esBarbero = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BARBERO"));

        List<CitaResponse> lista = citaService.consultarCitas(
                idCliente, idBarbero, estado, fechaInicio, fechaFin, authentication.getName(), esAdmin, esBarbero);

        return ResponseEntity.ok(ApiResponse.success("Listado de citas obtenido.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CitaResponse>> obtenerPorId(
            @PathVariable Integer id,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean esBarbero = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BARBERO"));

        CitaResponse response = citaService.obtenerPorId(id, authentication.getName(), esAdmin, esBarbero);
        return ResponseEntity.ok(ApiResponse.success("Detalle de cita obtenido.", response));
    }

    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<ApiResponse<CitaResponse>> reprogramarCita(
            @PathVariable Integer id,
            @Valid @RequestBody ReprogramarCitaRequest request,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        CitaResponse response = citaService.reprogramarCita(id, request, authentication.getName(), esAdmin);
        return ResponseEntity.ok(ApiResponse.success("Cita reprogramada exitosamente.", response));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<CitaResponse>> actualizarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambiarEstadoCitaRequest request,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        CitaResponse response = citaService.actualizarEstado(id, request, authentication.getName(), esAdmin);
        return ResponseEntity.ok(ApiResponse.success("Estado de cita actualizado exitosamente.", response));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<CitaResponse>> cancelarCita(
            @PathVariable Integer id,
            @Valid @RequestBody CancelarCitaRequest request,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        CitaResponse response = citaService.cancelarCita(id, request, authentication.getName(), esAdmin);
        return ResponseEntity.ok(ApiResponse.success("Cita cancelada correctamente.", response));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<HistorialCitaResponse>>> obtenerHistorial(
            @PathVariable Integer id,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<HistorialCitaResponse> historial = citaService.obtenerHistorialCita(id, authentication.getName(), esAdmin);
        return ResponseEntity.ok(ApiResponse.success("Historial de modificaciones de la cita.", historial));
    }
}

