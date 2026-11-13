package com.asbarbershop.pago.service;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.cita.entity.Cita;
import com.asbarbershop.cita.repository.CitaRepository;
import com.asbarbershop.cliente.entity.Cliente;
import com.asbarbershop.cliente.repository.ClienteRepository;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import com.asbarbershop.pago.dto.MetodoPagoResponse;
import com.asbarbershop.pago.dto.PagoResponse;
import com.asbarbershop.pago.dto.RegistrarPagoRequest;
import com.asbarbershop.pago.entity.MetodoPago;
import com.asbarbershop.pago.entity.Pago;
import com.asbarbershop.pago.repository.MetodoPagoRepository;
import com.asbarbershop.pago.repository.PagoRepository;
import com.asbarbershop.usuario.entity.Usuario;
import com.asbarbershop.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final BarberoRepository barberoRepository;

    @Transactional(readOnly = true)
    public List<MetodoPagoResponse> listarMetodosActivos() {
        return metodoPagoRepository.findByActivoTrue().stream()
                .map(m -> MetodoPagoResponse.builder()
                        .idMetodo(m.getIdMetodo())
                        .nombre(m.getNombre())
                        .activo(m.getActivo())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public PagoResponse registrarPago(RegistrarPagoRequest request, String emailUsuario) {
        Cita cita = citaRepository.findById(request.getIdCita())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + request.getIdCita()));

        if ("CANCELADA".equalsIgnoreCase(cita.getEstado().getNombre())) {
            throw new IllegalStateException("No se puede registrar un pago para una cita cancelada.");
        }

        MetodoPago metodo = metodoPagoRepository.findById(request.getIdMetodo())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado con ID: " + request.getIdMetodo()));

        if (!Boolean.TRUE.equals(metodo.getActivo())) {
            throw new IllegalArgumentException("El método de pago seleccionado no está activo.");
        }

        Usuario registradoPor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + emailUsuario));

        Pago pago = new Pago();
        pago.setCita(cita);
        pago.setMetodoPago(metodo);
        pago.setMonto(request.getMonto());
        pago.setReferencia(request.getReferencia());
        pago.setEstadoPago("CONFIRMADO");
        pago.setRegistradoPor(registradoPor);

        Pago guardado = pagoRepository.save(pago);
        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> consultarPagos(
            Integer idCita,
            Integer idMetodo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String emailUsuario,
            boolean esAdmin,
            boolean esBarbero) {

        Integer idClienteFiltro = null;
        Integer idBarberoFiltro = null;

        if (!esAdmin) {
            if (esBarbero) {
                Barbero barbero = barberoRepository.findByUsuarioEmail(emailUsuario)
                        .orElseThrow(() -> new ResourceNotFoundException("Barbero no encontrado para usuario actual."));
                idBarberoFiltro = barbero.getIdBarbero();
            } else {
                Cliente cliente = clienteRepository.findByUsuarioEmail(emailUsuario)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para usuario actual."));
                idClienteFiltro = cliente.getIdCliente();
            }
        }

        LocalDateTime dtInicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime dtFin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        List<Pago> pagos = pagoRepository.filtrarPagos(idCita, idClienteFiltro, idBarberoFiltro, idMetodo, dtInicio, dtFin);
        return pagos.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PagoResponse mapToResponse(Pago p) {
        String nombreCliente = p.getCita().getCliente().getUsuario().getNombre() + " " + p.getCita().getCliente().getUsuario().getApellido();
        String nombreBarbero = p.getCita().getBarbero().getUsuario().getNombre() + " " + p.getCita().getBarbero().getUsuario().getApellido();
        String registradoPor = p.getRegistradoPor().getNombre() + " " + p.getRegistradoPor().getApellido();

        return PagoResponse.builder()
                .idPago(p.getIdPago())
                .idCita(p.getCita().getIdCita())
                .nombreCliente(nombreCliente)
                .nombreBarbero(nombreBarbero)
                .idMetodo(p.getMetodoPago().getIdMetodo())
                .metodoPago(p.getMetodoPago().getNombre())
                .monto(p.getMonto())
                .referencia(p.getReferencia())
                .estadoPago(p.getEstadoPago())
                .registradoPor(registradoPor)
                .fechaPago(p.getFechaPago())
                .build();
    }
}

