package com.asbarbershop.horario.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.horario.dto.DisponibilidadResponse;
import com.asbarbershop.horario.dto.HorarioBarberoResponse;
import com.asbarbershop.horario.service.HorarioBarberoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioBarberoService horarioBarberoService;

    @GetMapping("/barbero/{idBarbero}")
    public ResponseEntity<ApiResponse<List<HorarioBarberoResponse>>> listarPorBarbero(@PathVariable Integer idBarbero) {
        List<HorarioBarberoResponse> lista = horarioBarberoService.listarPorBarbero(idBarbero);
        return ResponseEntity.ok(ApiResponse.success("Horarios del barbero obtenidos.", lista));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<DisponibilidadResponse>> consultarDisponibilidad(
            @RequestParam Integer idBarbero,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false, defaultValue = "30") Integer duracionMinutos) {
        DisponibilidadResponse response = horarioBarberoService.obtenerDisponibilidad(idBarbero, fecha, duracionMinutos);
        return ResponseEntity.ok(ApiResponse.success("Disponibilidad consultada con éxito.", response));
    }
}

