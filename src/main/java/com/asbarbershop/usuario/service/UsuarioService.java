package com.asbarbershop.usuario.service;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.cliente.entity.Cliente;
import com.asbarbershop.cliente.repository.ClienteRepository;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import com.asbarbershop.usuario.dto.*;
import com.asbarbershop.usuario.entity.Rol;
import com.asbarbershop.usuario.entity.Usuario;
import com.asbarbershop.usuario.repository.RolRepository;
import com.asbarbershop.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final BarberoRepository barberoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios(String rol, Boolean activo) {
        List<Usuario> usuarios = usuarioRepository.findAll();

        return usuarios.stream()
                .filter(u -> rol == null || u.getRol().getNombre().equalsIgnoreCase(rol))
                .filter(u -> activo == null || u.getActivo().equals(activo))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + email));
        return mapToResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crearUsuarioPorAdmin(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }

        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (usuarioRepository.existsByTelefono(request.getTelefono())) {
                throw new IllegalArgumentException("El teléfono ya se encuentra registrado.");
            }
        }

        Rol rol = rolRepository.findByNombre(request.getRol().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("El rol '" + request.getRol() + "' no es válido."));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setEmail(request.getEmail().trim().toLowerCase());
        usuario.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        if ("CLIENTE".equalsIgnoreCase(rol.getNombre())) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuarioGuardado);
            cliente.setPreferencias(request.getPreferencias());
            cliente.setFechaRegistro(LocalDate.now());
            clienteRepository.save(cliente);
        } else if ("BARBERO".equalsIgnoreCase(rol.getNombre())) {
            Barbero barbero = new Barbero();
            barbero.setUsuario(usuarioGuardado);
            barbero.setEspecialidad(request.getEspecialidad() != null ? request.getEspecialidad() : "Estilista General");
            barbero.setPorcentajeComision(request.getPorcentajeComision() != null ? request.getPorcentajeComision() : BigDecimal.ZERO);
            barbero.setDisponible(true);
            barberoRepository.save(barbero);
        }

        return mapToResponse(usuarioGuardado);
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(String email, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + email));

        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (!request.getTelefono().equals(usuario.getTelefono()) && usuarioRepository.existsByTelefono(request.getTelefono())) {
                throw new IllegalArgumentException("El teléfono ya está siendo usado por otro usuario.");
            }
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        usuarioRepository.save(usuario);

        if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre()) && request.getPreferencias() != null) {
            clienteRepository.findById(usuario.getIdUsuario()).ifPresent(cliente -> {
                cliente.setPreferencias(request.getPreferencias());
                clienteRepository.save(cliente);
            });
        }

        return mapToResponse(usuario);
    }

    @Transactional
    public void cambiarPassword(String email, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + email));

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarUsuarioPorAdmin(Integer id, ActualizarUsuarioAdminRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (!request.getTelefono().equals(usuario.getTelefono()) && usuarioRepository.existsByTelefono(request.getTelefono())) {
                throw new IllegalArgumentException("El teléfono ya está en uso.");
            }
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);

        if (request.getRol() != null && !request.getRol().trim().isEmpty()) {
            Rol nuevoRol = rolRepository.findByNombre(request.getRol().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("El rol '" + request.getRol() + "' no es válido."));
            usuario.setRol(nuevoRol);
        }

        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        usuarioRepository.save(usuario);

        // Actualizar datos de barbero si aplica
        if ("BARBERO".equalsIgnoreCase(usuario.getRol().getNombre())) {
            Barbero barbero = barberoRepository.findById(usuario.getIdUsuario())
                    .orElseGet(() -> {
                        Barbero nuevo = new Barbero();
                        nuevo.setUsuario(usuario);
                        return nuevo;
                    });
            if (request.getEspecialidad() != null) barbero.setEspecialidad(request.getEspecialidad());
            if (request.getPorcentajeComision() != null) barbero.setPorcentajeComision(request.getPorcentajeComision());
            if (request.getDisponible() != null) barbero.setDisponible(request.getDisponible());
            barberoRepository.save(barbero);
        }

        // Actualizar datos de cliente si aplica
        if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            Cliente cliente = clienteRepository.findById(usuario.getIdUsuario())
                    .orElseGet(() -> {
                        Cliente nuevo = new Cliente();
                        nuevo.setUsuario(usuario);
                        nuevo.setFechaRegistro(LocalDate.now());
                        return nuevo;
                    });
            if (request.getPreferencias() != null) cliente.setPreferencias(request.getPreferencias());
            clienteRepository.save(cliente);
        }

        return mapToResponse(usuario);
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo, String emailAdminEjecutor) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuario.getEmail().equalsIgnoreCase(emailAdminEjecutor)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta de administrador.");
        }

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Integer id, String emailAdminEjecutor) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuario.getEmail().equalsIgnoreCase(emailAdminEjecutor)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta de administrador.");
        }

        try {
            if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
                clienteRepository.deleteById(id);
            } else if ("BARBERO".equalsIgnoreCase(usuario.getRol().getNombre())) {
                barberoRepository.deleteById(id);
            }
            usuarioRepository.delete(usuario);
        } catch (Exception ex) {
            throw new IllegalStateException("No se puede eliminar el usuario porque tiene registros vinculados (citas, pagos o comisiones). Utiliza la desactivación en su lugar.");
        }
    }

    public UsuarioResponse mapToResponse(Usuario usuario) {
        UsuarioResponse.UsuarioResponseBuilder builder = UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol().getNombre())
                .activo(usuario.getActivo())
                .creadoEn(usuario.getCreadoEn());

        if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            clienteRepository.findById(usuario.getIdUsuario()).ifPresent(cliente -> {
                builder.preferencias(cliente.getPreferencias());
                builder.fechaRegistro(cliente.getFechaRegistro());
            });
        } else if ("BARBERO".equalsIgnoreCase(usuario.getRol().getNombre())) {
            barberoRepository.findById(usuario.getIdUsuario()).ifPresent(barbero -> {
                builder.especialidad(barbero.getEspecialidad());
                builder.porcentajeComision(barbero.getPorcentajeComision());
                builder.disponible(barbero.getDisponible());
            });
        }

        return builder.build();
    }
}

