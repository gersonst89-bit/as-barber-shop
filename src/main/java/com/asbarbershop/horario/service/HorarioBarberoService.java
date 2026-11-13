package com.asbarbershop.horario.service;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.cita.entity.Cita;
import com.asbarbershop.cita.repository.CitaRepository;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import com.asbarbershop.horario.dto.DisponibilidadResponse;
import com.asbarbershop.horario.dto.HorarioBarberoRequest;
import com.asbarbershop.horario.dto.HorarioBarberoResponse;
import com.asbarbershop.horario.entity.HorarioBarbero;
import com.asbarbershop.horario.repository.HorarioBarberoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HorarioBarberoService {

    private final HorarioBarberoRepository horarioBarberoRepository;
    private final BarberoRepository barberoRepository;
    private final CitaRepository citaRepository;

    private static final String[] DIAS_SEMANA = {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    @Transactional(readOnly = true)
    public List<HorarioBarberoResponse> listarPorBarbero(Integer idBarbero) {
        if (!barberoRepository.existsById(idBarbero)) {
            throw new ResourceNotFoundException("Barbero no encontrado con ID: " + idBarbero);
        }

        return horarioBarberoRepository.findByBarberoIdBarberoOrderByDiaSemanaAscHoraInicioAsc(idBarbero).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HorarioBarberoResponse registrarHorario(Integer idBarbero, HorarioBarberoRequest request) {
        Barbero barbero = barberoRepository.findById(idBarbero)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado con ID: " + idBarbero));

        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        HorarioBarbero horario = new HorarioBarbero();
        horario.setBarbero(barbero);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setActivo(request.getActivo() != null ? request.getActivo() : true);

        HorarioBarbero guardado = horarioBarberoRepository.save(horario);
        return mapToResponse(guardado);
    }

    @Transactional
    public HorarioBarberoResponse actualizarHorario(Integer idHorario, HorarioBarberoRequest request) {
        HorarioBarbero horario = horarioBarberoRepository.findById(idHorario)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con ID: " + idHorario));

        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        if (request.getActivo() != null) {
            horario.setActivo(request.getActivo());
        }

        HorarioBarbero guardado = horarioBarberoRepository.save(horario);
        return mapToResponse(guardado);
    }

    @Transactional
    public void cambiarEstado(Integer idHorario, boolean activo) {
        HorarioBarbero horario = horarioBarberoRepository.findById(idHorario)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con ID: " + idHorario));

        horario.setActivo(activo);
        horarioBarberoRepository.save(horario);
    }

    @Transactional
    public void eliminarHorario(Integer idHorario) {
        HorarioBarbero horario = horarioBarberoRepository.findById(idHorario)
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado con ID: " + idHorario));
        horarioBarberoRepository.delete(horario);
    }

    @Transactional(readOnly = true)
    public DisponibilidadResponse obtenerDisponibilidad(Integer idBarbero, LocalDate fecha, Integer duracionMinutos) {
        Barbero barbero = barberoRepository.findById(idBarbero)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado con ID: " + idBarbero));

        if (duracionMinutos == null || duracionMinutos <= 0) {
            duracionMinutos = 30; // Duración por defecto de 30 min
        }

        // Obtener día de la semana (1 = Lunes .. 7 = Domingo)
        byte diaSemana = (byte) fecha.getDayOfWeek().getValue();
        String nombreDia = DIAS_SEMANA[diaSemana - 1];

        Optional<HorarioBarbero> horarioOpt =
                horarioBarberoRepository.findByBarberoIdBarberoAndDiaSemanaAndActivoTrue(idBarbero, diaSemana);

        if (horarioOpt.isEmpty()) {
            return DisponibilidadResponse.builder()
                    .idBarbero(idBarbero)
                    .nombreBarbero(barbero.getUsuario().getNombre() + " " + barbero.getUsuario().getApellido())
                    .fecha(fecha)
                    .diaSemana(nombreDia)
                    .tieneHorario(false)
                    .horasDisponibles(List.of())
                    .build();
        }

        HorarioBarbero horario = horarioOpt.get();
        List<Cita> citasDelDia = citaRepository.findByBarberoIdBarberoAndFechaOrderByHoraInicioAsc(idBarbero, fecha)
                .stream()
                .filter(c -> !"CANCELADA".equalsIgnoreCase(c.getEstado().getNombre()))
                .collect(Collectors.toList());

        List<LocalTime> horasDisponibles = new ArrayList<>();
        LocalTime slot = horario.getHoraInicio();
        LocalTime limiteFin = horario.getHoraFin();
        LocalTime ahora = LocalTime.now();
        boolean esHoy = fecha.isEqual(LocalDate.now());

        while (!slot.plusMinutes(duracionMinutos).isAfter(limiteFin)) {
            LocalTime slotFin = slot.plusMinutes(duracionMinutos);

            // Si es hoy, no sugerir horarios pasados
            if (!esHoy || slot.isAfter(ahora)) {
                final LocalTime sInicio = slot;
                final LocalTime sFin = slotFin;

                boolean hayConflicto = citasDelDia.stream().anyMatch(c ->
                        sInicio.isBefore(c.getHoraFin()) && sFin.isAfter(c.getHoraInicio())
                );

                if (!hayConflicto) {
                    horasDisponibles.add(slot);
                }
            }

            slot = slot.plusMinutes(30); // Intervalos de 30 min
        }

        return DisponibilidadResponse.builder()
                .idBarbero(idBarbero)
                .nombreBarbero(barbero.getUsuario().getNombre() + " " + barbero.getUsuario().getApellido())
                .fecha(fecha)
                .diaSemana(nombreDia)
                .tieneHorario(true)
                .horarioApertura(horario.getHoraInicio())
                .horarioCierre(horario.getHoraFin())
                .horasDisponibles(horasDisponibles)
                .build();
    }

    public HorarioBarberoResponse mapToResponse(HorarioBarbero horario) {
        String nombreDia = (horario.getDiaSemana() >= 1 && horario.getDiaSemana() <= 7)
                ? DIAS_SEMANA[horario.getDiaSemana() - 1]
                : "Desconocido";

        return HorarioBarberoResponse.builder()
                .idHorario(horario.getIdHorario())
                .idBarbero(horario.getBarbero().getIdBarbero())
                .nombreBarbero(horario.getBarbero().getUsuario().getNombre() + " " + horario.getBarbero().getUsuario().getApellido())
                .diaSemana(horario.getDiaSemana())
                .nombreDiaSemana(nombreDia)
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .activo(horario.getActivo())
                .build();
    }
}

