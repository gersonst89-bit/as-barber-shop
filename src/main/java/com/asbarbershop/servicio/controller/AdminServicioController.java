package com.asbarbershop.servicio.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.servicio.dto.AsignarServicioRequest;
import com.asbarbershop.servicio.dto.ServicioRequest;
import com.asbarbershop.servicio.dto.ServicioResponse;
import com.asbarbershop.servicio.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarTodos() {
        List<ServicioResponse> lista = servicioService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success("Listado completo de servicios.", lista));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServicioResponse>> crearServicio(@Valid @RequestBody ServicioRequest request) {
        ServicioResponse response = servicioService.crearServicio(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Servicio creado exitosamente.", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> actualizarServicio(
            @PathVariable Integer id,
            @Valid @RequestBody ServicioRequest request) {
        ServicioResponse response = servicioService.actualizarServicio(id, request);
        return ResponseEntity.ok(ApiResponse.success("Servicio actualizado exitosamente.", response));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo) {
        servicioService.cambiarEstado(id, activo);
        String msg = activo ? "Servicio activado." : "Servicio desactivado.";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarServicio(@PathVariable Integer id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio eliminado físicamente de la base de datos.", null));
    }

    @PostMapping("/asignar-barbero")
    public ResponseEntity<ApiResponse<Void>> asignarServicioABarbero(@Valid @RequestBody AsignarServicioRequest request) {
        servicioService.asociarServicioABarbero(request.getIdBarbero(), request.getIdServicio());
        return ResponseEntity.ok(ApiResponse.success("Servicio asignado al barbero correctamente.", null));
    }

    @DeleteMapping("/barbero/{idBarbero}/servicio/{idServicio}")
    public ResponseEntity<ApiResponse<Void>> desasociarServicioDeBarbero(
            @PathVariable Integer idBarbero,
            @PathVariable Integer idServicio) {
        servicioService.desasociarServicioDeBarbero(idBarbero, idServicio);
        return ResponseEntity.ok(ApiResponse.success("Servicio desasociado del barbero.", null));
    }
}

