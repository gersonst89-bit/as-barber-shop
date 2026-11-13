package com.asbarbershop.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean exitoso;

    private String mensaje;

    private T datos;

    private LocalDateTime fecha;

    public static <T> ApiResponse<T> success(String mensaje, T datos) {

        return new ApiResponse<>(
                true,
                mensaje,
                datos,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> error(String mensaje) {

        return new ApiResponse<>(
                false,
                mensaje,
                null,
                LocalDateTime.now()
        );
    }
}