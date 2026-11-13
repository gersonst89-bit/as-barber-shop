package com.asbarbershop.servicio.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.servicio.dto.ServicioResponse;
import com.asbarbershop.servicio.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarActivos() {
        List<ServicioResponse> lista = servicioService.listarActivos();
        return ResponseEntity.ok(ApiResponse.success("Catálogo de servicios obtenido.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> obtenerPorId(@PathVariable Integer id) {
        ServicioResponse response = servicioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio encontrado.", response));
    }

    @GetMapping("/barbero/{idBarbero}")
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarPorBarbero(@PathVariable Integer idBarbero) {
        List<ServicioResponse> lista = servicioService.listarServiciosPorBarbero(idBarbero);
        return ResponseEntity.ok(ApiResponse.success("Servicios ofrecidos por el barbero obtenidos.", lista));
    }
}

