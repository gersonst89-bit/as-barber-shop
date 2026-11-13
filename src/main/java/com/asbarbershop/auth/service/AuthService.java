package com.asbarbershop.auth.service;

import com.asbarbershop.auth.dto.AuthResponse;
import com.asbarbershop.auth.dto.LoginRequest;
import com.asbarbershop.auth.dto.RegistroClienteRequest;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.cliente.entity.Cliente;
import com.asbarbershop.cliente.repository.ClienteRepository;
import com.asbarbershop.security.CustomUserDetailsService;
import com.asbarbershop.security.JwtService;
import com.asbarbershop.usuario.entity.Rol;
import com.asbarbershop.usuario.entity.Usuario;
import com.asbarbershop.usuario.repository.RolRepository;
import com.asbarbershop.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final BarberoRepository barberoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Correo o contraseña incorrectos.");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("La cuenta se encuentra inactiva. Contacta al administrador.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generateToken(userDetails);

        Integer idPerfil = null;
        String nombreRol = usuario.getRol().getNombre();
        if ("CLIENTE".equalsIgnoreCase(nombreRol)) {
            idPerfil = clienteRepository.findByUsuarioEmail(usuario.getEmail())
                    .map(Cliente::getIdCliente)
                    .orElse(usuario.getIdUsuario());
        } else if ("BARBERO".equalsIgnoreCase(nombreRol)) {
            idPerfil = barberoRepository.findByUsuarioEmail(usuario.getEmail())
                    .map(b -> b.getIdBarbero())
                    .orElse(usuario.getIdUsuario());
        }

        return AuthResponse.builder()
                .token(token)
                .tipoToken("Bearer")
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(nombreRol)
                .idPerfil(idPerfil)
                .build();
    }

    @Transactional
    public AuthResponse registrarCliente(RegistroClienteRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }

        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (usuarioRepository.existsByTelefono(request.getTelefono())) {
                throw new IllegalArgumentException("El teléfono ya se encuentra registrado en el sistema.");
            }
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalStateException("El rol CLIENTE no está configurado en el sistema."));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setEmail(request.getEmail().trim().toLowerCase());
        usuario.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rolCliente);
        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuarioGuardado);
        cliente.setPreferencias(request.getPreferencias());
        cliente.setFechaRegistro(LocalDate.now());

        clienteRepository.save(cliente);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuarioGuardado.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tipoToken("Bearer")
                .idUsuario(usuarioGuardado.getIdUsuario())
                .nombre(usuarioGuardado.getNombre())
                .apellido(usuarioGuardado.getApellido())
                .email(usuarioGuardado.getEmail())
                .rol("CLIENTE")
                .idPerfil(usuarioGuardado.getIdUsuario())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse obtenerUsuarioActual(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con correo: " + email));

        Integer idPerfil = null;
        String nombreRol = usuario.getRol().getNombre();
        if ("CLIENTE".equalsIgnoreCase(nombreRol)) {
            idPerfil = clienteRepository.findByUsuarioEmail(usuario.getEmail())
                    .map(Cliente::getIdCliente)
                    .orElse(usuario.getIdUsuario());
        } else if ("BARBERO".equalsIgnoreCase(nombreRol)) {
            idPerfil = barberoRepository.findByUsuarioEmail(usuario.getEmail())
                    .map(b -> b.getIdBarbero())
                    .orElse(usuario.getIdUsuario());
        }

        return AuthResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(nombreRol)
                .idPerfil(idPerfil)
                .build();
    }
}

