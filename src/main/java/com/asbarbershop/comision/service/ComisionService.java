package com.asbarbershop.comision.service;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.cita.entity.Cita;
import com.asbarbershop.comision.dto.ComisionResponse;
import com.asbarbershop.comision.entity.Comision;
import com.asbarbershop.comision.repository.ComisionRepository;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComisionService {

    private final ComisionRepository comisionRepository;
    private final BarberoRepository barberoRepository;

    @Transactional
    public Optional<Comision> generarComisionParaCita(Cita cita) {
        if (comisionRepository.existsByCitaIdCita(cita.getIdCita())) {
            return comisionRepository.findByCitaIdCita(cita.getIdCita());
        }

        Barbero barbero = cita.getBarbero();
        BigDecimal porcentaje = barbero.getPorcentajeComision() != null
                ? barbero.getPorcentajeComision()
                : BigDecimal.ZERO;

        BigDecimal montoBase = cita.getMontoTotal() != null
                ? cita.getMontoTotal()
                : BigDecimal.ZERO;

        BigDecimal montoComision = montoBase.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Comision comision = new Comision();
        comision.setCita(cita);
        comision.setBarbero(barbero);
        comision.setMontoBase(montoBase);
        comision.setPorcentaje(porcentaje);
        comision.setMontoComision(montoComision);
        comision.setEstado("PENDIENTE");

        Comision guardada = comisionRepository.save(comision);
        return Optional.of(guardada);
    }

    @Transactional(readOnly = true)
    public List<ComisionResponse> consultarComisiones(
            Integer idBarbero,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String emailUsuario,
            boolean esAdmin) {

        Integer idBarberoFiltro = idBarbero;
        if (!esAdmin) {
            Barbero barberoActual = barberoRepository.findByUsuarioEmail(emailUsuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado para el usuario actual."));
            idBarberoFiltro = barberoActual.getIdBarbero();
        }

        LocalDateTime dtInicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime dtFin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        List<Comision> lista = comisionRepository.filtrarComisiones(idBarberoFiltro, estado, dtInicio, dtFin);
        return lista.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ComisionResponse cambiarEstado(Integer idComision, String nuevoEstado) {
        Comision comision = comisionRepository.findById(idComision)
                .orElseThrow(() -> new ResourceNotFoundException("Comisión no encontrada con ID: " + idComision));

        comision.setEstado(nuevoEstado.toUpperCase());
        Comision guardada = comisionRepository.save(comision);
        return mapToResponse(guardada);
    }

    public ComisionResponse mapToResponse(Comision c) {
        String nombreBarbero = c.getBarbero().getUsuario().getNombre() + " " + c.getBarbero().getUsuario().getApellido();

        return ComisionResponse.builder()
                .idComision(c.getIdComision())
                .idCita(c.getCita().getIdCita())
                .idBarbero(c.getBarbero().getIdBarbero())
                .nombreBarbero(nombreBarbero)
                .montoBase(c.getMontoBase())
                .porcentaje(c.getPorcentaje())
                .montoComision(c.getMontoComision())
                .estado(c.getEstado())
                .creadoEn(c.getCreadoEn())
                .build();
    }
}

