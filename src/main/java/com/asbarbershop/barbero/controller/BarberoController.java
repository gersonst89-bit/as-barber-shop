package com.asbarbershop.barbero.controller;

import com.asbarbershop.barbero.dto.BarberoResponse;
import com.asbarbershop.barbero.service.BarberoService;
import com.asbarbershop.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barberos")
@RequiredArgsConstructor
public class BarberoController {

    private final BarberoService barberoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BarberoResponse>>> listarBarberosActivos() {
        List<BarberoResponse> lista = barberoService.listarBarberosActivos();
        return ResponseEntity.ok(ApiResponse.success("Listado de barberos disponibles obtenido.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BarberoResponse>> obtenerBarberoPorId(@PathVariable Integer id) {
        BarberoResponse response = barberoService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Barbero encontrado.", response));
    }
}
