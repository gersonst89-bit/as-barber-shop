package com.asbarbershop.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Manejar recursos que no existen (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Manejar argumentos inválidos o reglas de negocio no cumplidas (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Manejar conflictos de estado de negocio (409)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Manejar credenciales incorrectas (401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos.",
                request.getRequestURI()
        );
    }

    // Manejar cuentas inactivas / desactivadas (403)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(
            DisabledException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "La cuenta se encuentra inactiva. Contacta al administrador.",
                request.getRequestURI()
        );
    }

    // Manejar accesos denegados por falta de rol o permisos (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para acceder a este recurso.",
                request.getRequestURI()
        );
    }

    // Manejar errores de validación de campos con @Valid (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = new HashMap<>();
        response.put("fecha", LocalDateTime.now());
        response.put("estado", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Error de validación");
        response.put("mensaje", "Verifica los datos ingresados.");
        response.put("ruta", request.getRequestURI());
        response.put("detalles", errores);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // Manejar recursos estáticos o páginas que no existen (404)
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "La página o recurso no existe: " + request.getRequestURI(),
                request.getRequestURI()
        );
    }

    // Manejar errores inesperados (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado en el servidor: " + ex.getMessage(),
                request.getRequestURI()
        );
    }

    // Construir una respuesta de error uniforme
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String mensaje,
            String ruta) {

        Map<String, Object> response = new HashMap<>();

        response.put("fecha", LocalDateTime.now());
        response.put("estado", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("mensaje", mensaje);
        response.put("ruta", ruta);

        return ResponseEntity.status(status).body(response);
    }
}
