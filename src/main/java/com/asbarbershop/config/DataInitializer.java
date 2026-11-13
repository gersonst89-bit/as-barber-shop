package com.asbarbershop.config;

import com.asbarbershop.cita.entity.EstadoCita;
import com.asbarbershop.cita.repository.EstadoCitaRepository;
import com.asbarbershop.pago.entity.MetodoPago;
import com.asbarbershop.pago.repository.MetodoPagoRepository;
import com.asbarbershop.usuario.entity.Rol;
import com.asbarbershop.usuario.entity.Usuario;
import com.asbarbershop.usuario.repository.RolRepository;
import com.asbarbershop.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        inicializarRoles();
        inicializarEstadosCita();
        inicializarMetodosPago();
        inicializarAdminPorDefecto();
    }

    private void inicializarRoles() {
        crearRolSiNoExiste("ADMIN", "Acceso total al sistema y administracion general");
        crearRolSiNoExiste("BARBERO", "Gestion de agenda, horarios y atencion de citas");
        crearRolSiNoExiste("CLIENTE", "Registro, gestion de perfil y reserva de citas");
    }

    private void crearRolSiNoExiste(String nombre, String descripcion) {
        if (!rolRepository.existsByNombre(nombre)) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            rolRepository.save(rol);
            log.info("Rol '{}' inicializado.", nombre);
        }
    }

    private void inicializarEstadosCita() {
        crearEstadoSiNoExiste("PENDIENTE", "#ffc107");
        crearEstadoSiNoExiste("CONFIRMADA", "#17a2b8");
        crearEstadoSiNoExiste("EN_PROCESO", "#007bff");
        crearEstadoSiNoExiste("ATENDIDA", "#28a745");
        crearEstadoSiNoExiste("CANCELADA", "#dc3545");
        crearEstadoSiNoExiste("AUSENTE", "#6f42c1");
    }

    private void crearEstadoSiNoExiste(String nombre, String colorHex) {
        if (estadoCitaRepository.findByNombre(nombre).isEmpty()) {
            EstadoCita estado = new EstadoCita();
            estado.setNombre(nombre);
            estado.setColorHex(colorHex);
            estadoCitaRepository.save(estado);
            log.info("Estado de cita '{}' inicializado.", nombre);
        }
    }

    private void inicializarMetodosPago() {
        crearMetodoPagoSiNoExiste("EFECTIVO");
        crearMetodoPagoSiNoExiste("TARJETA");
        crearMetodoPagoSiNoExiste("YAPE");
        crearMetodoPagoSiNoExiste("PLIN");
        crearMetodoPagoSiNoExiste("TRANSFERENCIA");
    }

    private void crearMetodoPagoSiNoExiste(String nombre) {
        if (metodoPagoRepository.findByNombre(nombre).isEmpty()) {
            MetodoPago metodo = new MetodoPago();
            metodo.setNombre(nombre);
            metodo.setActivo(true);
            metodoPagoRepository.save(metodo);
            log.info("Método de pago '{}' inicializado.", nombre);
        }
    }

    private void inicializarAdminPorDefecto() {
        String adminEmail = "admin@asbarbershop.com";
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElse(null);
            if (rolAdmin != null) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("General");
                admin.setEmail(adminEmail);
                admin.setTelefono("999888777");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setRol(rolAdmin);
                admin.setActivo(true);
                usuarioRepository.save(admin);
                log.info("Usuario ADMIN por defecto creado: {} / admin123", adminEmail);
            }
        }
    }
}

