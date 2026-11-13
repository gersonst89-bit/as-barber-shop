package com.asbarbershop.cita.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CrearCitaRequest {

    // Opcional si el cliente autenticado realiza su propia reserva
    private Integer idCliente;

    @NotNull(message = "El ID del barbero es obligatorio.")
    private Integer idBarbero;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotEmpty(message = "Debes seleccionar al menos un servicio.")
    private List<Integer> idsServicios;

    @Size(max = 30, message = "El código promocional no puede exceder 30 caracteres.")
    private String codigoPromocion;

    @Size(max = 255, message = "Las notas no pueden exceder 255 caracteres.")
    private String notas;
}

