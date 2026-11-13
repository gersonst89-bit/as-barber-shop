package com.asbarbershop.usuario.controller;

import com.asbarbershop.common.response.ApiResponse;
import com.asbarbershop.usuario.dto.ActualizarUsuarioAdminRequest;
import com.asbarbershop.usuario.dto.CrearUsuarioRequest;
import com.asbarbershop.usuario.dto.UsuarioResponse;
import com.asbarbershop.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarUsuarios(
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo) {
        List<UsuarioResponse> lista = usuarioService.listarUsuarios(rol, activo);
        return ResponseEntity.ok(ApiResponse.success("Listado de usuarios obtenido.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerUsuarioPorId(@PathVariable Integer id) {
        UsuarioResponse response = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario encontrado.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioResponse response = usuarioService.crearUsuarioPorAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario creado exitosamente.", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarUsuarioAdminRequest request) {
        UsuarioResponse response = usuarioService.actualizarUsuarioPorAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado correctamente.", response));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo,
            Authentication authentication) {
        usuarioService.cambiarEstado(id, activo, authentication.getName());
        String msg = activo ? "Usuario activado exitosamente." : "Usuario desactivado exitosamente.";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarUsuario(
            @PathVariable Integer id,
            Authentication authentication) {
        usuarioService.eliminarUsuario(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado físicamente de la base de datos.", null));
    }
}

