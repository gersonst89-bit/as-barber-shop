package com.asbarbershop.cliente.service;

import com.asbarbershop.cliente.dto.ClienteResponse;
import com.asbarbershop.cliente.entity.Cliente;
import com.asbarbershop.cliente.repository.ClienteRepository;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToResponse(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorEmail(String email) {
        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con correo: " + email));
        return mapToResponse(cliente);
    }

    public ClienteResponse mapToResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getUsuario().getNombre())
                .apellido(cliente.getUsuario().getApellido())
                .telefono(cliente.getUsuario().getTelefono())
                .email(cliente.getUsuario().getEmail())
                .preferencias(cliente.getPreferencias())
                .fechaRegistro(cliente.getFechaRegistro())
                .activo(cliente.getUsuario().getActivo())
                .build();
    }
}

