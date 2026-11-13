package com.asbarbershop.horario.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.horario.dto.HorarioBarberoRequest;
import com.asbarbershop.horario.dto.HorarioBarberoResponse;
import com.asbarbershop.horario.service.HorarioBarberoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/horarios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BARBERO')")
public class AdminHorarioController {

    private final HorarioBarberoService horarioBarberoService;

    @PostMapping("/barbero/{idBarbero}")
    public ResponseEntity<ApiResponse<HorarioBarberoResponse>> registrarHorario(
            @PathVariable Integer idBarbero,
            @Valid @RequestBody HorarioBarberoRequest request) {
        HorarioBarberoResponse response = horarioBarberoService.registrarHorario(idBarbero, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Horario registrado exitosamente.", response));
    }

    @PutMapping("/{idHorario}")
    public ResponseEntity<ApiResponse<HorarioBarberoResponse>> actualizarHorario(
            @PathVariable Integer idHorario,
            @Valid @RequestBody HorarioBarberoRequest request) {
        HorarioBarberoResponse response = horarioBarberoService.actualizarHorario(idHorario, request);
        return ResponseEntity.ok(ApiResponse.success("Horario actualizado exitosamente.", response));
    }

    @PatchMapping("/{idHorario}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer idHorario,
            @RequestParam boolean activo) {
        horarioBarberoService.cambiarEstado(idHorario, activo);
        String msg = activo ? "Horario activado." : "Horario desactivado.";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @DeleteMapping("/{idHorario}")
    public ResponseEntity<ApiResponse<Void>> eliminarHorario(@PathVariable Integer idHorario) {
        horarioBarberoService.eliminarHorario(idHorario);
        return ResponseEntity.ok(ApiResponse.success("Horario eliminado exitosamente.", null));
    }
}

