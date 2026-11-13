package com.asbarbershop.servicio.service;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import com.asbarbershop.servicio.dto.ServicioRequest;
import com.asbarbershop.servicio.dto.ServicioResponse;
import com.asbarbershop.servicio.entity.BarberoServicio;
import com.asbarbershop.servicio.entity.BarberoServicioId;
import com.asbarbershop.servicio.entity.Servicio;
import com.asbarbershop.servicio.repository.BarberoServicioRepository;
import com.asbarbershop.servicio.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final BarberoRepository barberoRepository;
    private final BarberoServicioRepository barberoServicioRepository;

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarActivos() {
        return servicioRepository.findByActivoTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarTodos() {
        return servicioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtenerPorId(Integer id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));
        return mapToResponse(servicio);
    }

    @Transactional
    public ServicioResponse crearServicio(ServicioRequest request) {
        if (servicioRepository.existsByNombre(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un servicio registrado con el nombre: " + request.getNombre());
        }

        Servicio servicio = new Servicio();
        servicio.setNombre(request.getNombre().trim());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setPrecio(request.getPrecio());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Servicio guardado = servicioRepository.save(servicio);
        return mapToResponse(guardado);
    }

    @Transactional
    public ServicioResponse actualizarServicio(Integer id, ServicioRequest request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        if (!servicio.getNombre().equalsIgnoreCase(request.getNombre().trim())
                && servicioRepository.existsByNombre(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe un servicio registrado con el nombre: " + request.getNombre());
        }

        servicio.setNombre(request.getNombre().trim());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setPrecio(request.getPrecio());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        if (request.getActivo() != null) {
            servicio.setActivo(request.getActivo());
        }

        Servicio guardado = servicioRepository.save(servicio);
        return mapToResponse(guardado);
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        servicio.setActivo(activo);
        servicioRepository.save(servicio);
    }

    @Transactional
    public void eliminarServicio(Integer id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + id));

        try {
            servicioRepository.delete(servicio);
        } catch (Exception ex) {
            throw new IllegalStateException("No se puede eliminar el servicio porque ya tiene citas o barberos asociados. Utiliza la desactivación lógica en su lugar.");
        }
    }

    @Transactional
    public void asociarServicioABarbero(Integer idBarbero, Integer idServicio) {
        Barbero barbero = barberoRepository.findById(idBarbero)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado con ID: " + idBarbero));

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + idServicio));

        BarberoServicioId id = new BarberoServicioId(idBarbero, idServicio);
        if (barberoServicioRepository.existsById(id)) {
            throw new IllegalArgumentException("El barbero ya tiene asignado este servicio.");
        }

        BarberoServicio relacion = new BarberoServicio();
        relacion.setId(id);
        relacion.setBarbero(barbero);
        relacion.setServicio(servicio);

        barberoServicioRepository.save(relacion);
    }

    @Transactional
    public void desasociarServicioDeBarbero(Integer idBarbero, Integer idServicio) {
        BarberoServicioId id = new BarberoServicioId(idBarbero, idServicio);
        if (!barberoServicioRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe la asignación de servicio para este barbero.");
        }
        barberoServicioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarServiciosPorBarbero(Integer idBarbero) {
        if (!barberoRepository.existsById(idBarbero)) {
            throw new ResourceNotFoundException("Barbero no encontrado con ID: " + idBarbero);
        }

        return barberoServicioRepository.findByBarberoIdBarbero(idBarbero).stream()
                .map(bs -> mapToResponse(bs.getServicio()))
                .filter(ServicioResponse::getActivo)
                .collect(Collectors.toList());
    }

    public ServicioResponse mapToResponse(Servicio servicio) {
        return ServicioResponse.builder()
                .idServicio(servicio.getIdServicio())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precio(servicio.getPrecio())
                .duracionMinutos(servicio.getDuracionMinutos())
                .activo(servicio.getActivo())
                .build();
    }
}

