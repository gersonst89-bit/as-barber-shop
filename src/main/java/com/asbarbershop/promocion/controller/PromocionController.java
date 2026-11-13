package com.asbarbershop.promocion.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.promocion.dto.PromocionResponse;
import com.asbarbershop.promocion.dto.ValidarPromocionResponse;
import com.asbarbershop.promocion.service.PromocionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promociones")
@RequiredArgsConstructor
public class PromocionController {

    private final PromocionService promocionService;

    @GetMapping("/vigentes")
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> listarVigentes() {
        List<PromocionResponse> lista = promocionService.listarVigentes();
        return ResponseEntity.ok(ApiResponse.success("Promociones vigentes obtenidas.", lista));
    }

    @GetMapping("/validar")
    public ResponseEntity<ApiResponse<ValidarPromocionResponse>> validarPromocion(
            @RequestParam String codigo,
            @RequestParam(required = false, defaultValue = "0") BigDecimal subtotal) {
        ValidarPromocionResponse response = promocionService.validarPromocion(codigo, subtotal);
        return ResponseEntity.ok(ApiResponse.success(response.getMensaje(), response));
    }
}

