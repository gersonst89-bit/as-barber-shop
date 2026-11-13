package com.asbarbershop.barbero.service;

import com.asbarbershop.barbero.dto.ActualizarBarberoRequest;
import com.asbarbershop.barbero.dto.BarberoResponse;
import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BarberoService {

    private final BarberoRepository barberoRepository;

    @Transactional(readOnly = true)
    public List<BarberoResponse> listarBarberosActivos() {
        return barberoRepository.findByDisponibleTrueAndUsuarioActivoTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BarberoResponse> listarTodos() {
        return barberoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BarberoResponse obtenerPorId(Integer id) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado con ID: " + id));
        return mapToResponse(barbero);
    }

    @Transactional
    public BarberoResponse actualizar(Integer id, ActualizarBarberoRequest request) {
        Barbero barbero = barberoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado con ID: " + id));

        if (request.getEspecialidad() != null) {
            barbero.setEspecialidad(request.getEspecialidad());
        }
        barbero.setPorcentajeComision(request.getPorcentajeComision());
        barbero.setDisponible(request.getDisponible());

        Barbero guardado = barberoRepository.save(barbero);
        return mapToResponse(guardado);
    }

    public BarberoResponse mapToResponse(Barbero barbero) {
        return BarberoResponse.builder()
                .idBarbero(barbero.getIdBarbero())
                .nombre(barbero.getUsuario().getNombre())
                .apellido(barbero.getUsuario().getApellido())
                .telefono(barbero.getUsuario().getTelefono())
                .email(barbero.getUsuario().getEmail())
                .especialidad(barbero.getEspecialidad())
                .porcentajeComision(barbero.getPorcentajeComision())
                .disponible(barbero.getDisponible())
                .activo(barbero.getUsuario().getActivo())
                .build();
    }
}

