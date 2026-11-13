package com.asbarbershop.pago.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.pago.dto.MetodoPagoResponse;
import com.asbarbershop.pago.dto.PagoResponse;
import com.asbarbershop.pago.dto.RegistrarPagoRequest;
import com.asbarbershop.pago.service.PagoService;
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
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping("/metodos")
    public ResponseEntity<ApiResponse<List<MetodoPagoResponse>>> listarMetodos() {
        List<MetodoPagoResponse> lista = pagoService.listarMetodosActivos();
        return ResponseEntity.ok(ApiResponse.success("Métodos de pago obtenidos.", lista));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponse>> registrarPago(
            @Valid @RequestBody RegistrarPagoRequest request,
            Authentication authentication) {

        PagoResponse response = pagoService.registrarPago(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pago registrado exitosamente.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoResponse>>> consultarPagos(
            @RequestParam(required = false) Integer idCita,
            @RequestParam(required = false) Integer idMetodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {

        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean esBarbero = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BARBERO"));

        List<PagoResponse> lista = pagoService.consultarPagos(
                idCita, idMetodo, fechaInicio, fechaFin, authentication.getName(), esAdmin, esBarbero);

        return ResponseEntity.ok(ApiResponse.success("Historial de pagos obtenido.", lista));
    }
}

