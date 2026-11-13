package com.asbarbershop.promocion.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.promocion.dto.PromocionRequest;
import com.asbarbershop.promocion.dto.PromocionResponse;
import com.asbarbershop.promocion.service.PromocionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promociones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromocionController {

    private final PromocionService promocionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> listarTodas() {
        List<PromocionResponse> lista = promocionService.listarTodas();
        return ResponseEntity.ok(ApiResponse.success("Listado de promociones obtenido.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> obtenerPorId(@PathVariable Integer id) {
        PromocionResponse response = promocionService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Promoción encontrada.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromocionResponse>> crearPromocion(@Valid @RequestBody PromocionRequest request) {
        PromocionResponse response = promocionService.crearPromocion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Promoción registrada exitosamente.", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> actualizarPromocion(
            @PathVariable Integer id,
            @Valid @RequestBody PromocionRequest request) {
        PromocionResponse response = promocionService.actualizarPromocion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Promoción actualizada exitosamente.", response));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo) {
        promocionService.cambiarEstado(id, activo);
        String msg = activo ? "Promoción activada." : "Promoción desactivada.";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPromocion(@PathVariable Integer id) {
        promocionService.eliminarPromocion(id);
        return ResponseEntity.ok(ApiResponse.success("Promoción eliminada físicamente de la base de datos.", null));
    }
}

