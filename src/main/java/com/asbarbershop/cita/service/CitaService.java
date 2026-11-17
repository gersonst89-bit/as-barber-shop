package com.asbarbershop.cita.service;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.barbero.repository.BarberoRepository;
import com.asbarbershop.cita.dto.*;
import com.asbarbershop.cita.entity.Cita;
import com.asbarbershop.cita.entity.DetalleCita;
import com.asbarbershop.cita.entity.EstadoCita;
import com.asbarbershop.cita.entity.HistorialCita;
import com.asbarbershop.cita.repository.CitaRepository;
import com.asbarbershop.cita.repository.DetalleCitaRepository;
import com.asbarbershop.cita.repository.EstadoCitaRepository;
import com.asbarbershop.cita.repository.HistorialCitaRepository;
import com.asbarbershop.cliente.entity.Cliente;
import com.asbarbershop.cliente.repository.ClienteRepository;
import com.asbarbershop.comision.service.ComisionService;
import com.asbarbershop.common.exception.ResourceNotFoundException;
import com.asbarbershop.horario.entity.HorarioBarbero;
import com.asbarbershop.horario.repository.HorarioBarberoRepository;
import com.asbarbershop.promocion.dto.ValidarPromocionResponse;
import com.asbarbershop.promocion.entity.Promocion;
import com.asbarbershop.promocion.repository.PromocionRepository;
import com.asbarbershop.promocion.service.PromocionService;
import com.asbarbershop.servicio.entity.Servicio;
import com.asbarbershop.servicio.repository.ServicioRepository;
import com.asbarbershop.usuario.entity.Usuario;
import com.asbarbershop.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final DetalleCitaRepository detalleCitaRepository;
    private final HistorialCitaRepository historialCitaRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final ClienteRepository clienteRepository;
    private final BarberoRepository barberoRepository;
    private final ServicioRepository servicioRepository;
    private final HorarioBarberoRepository horarioBarberoRepository;
    private final PromocionRepository promocionRepository;
    private final PromocionService promocionService;
    private final UsuarioRepository usuarioRepository;
    private final ComisionService comisionService;

    @Transactional
    public CitaResponse crearCita(CrearCitaRequest request, String emailUsuario) {
        Usuario usuarioCreador = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario creador no encontrado: " + emailUsuario));

        // Determinar el cliente
        Cliente cliente;
        if ("CLIENTE".equalsIgnoreCase(usuarioCreador.getRol().getNombre())) {
            cliente = clienteRepository.findByUsuarioEmail(emailUsuario)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Perfil de cliente no encontrado para: " + emailUsuario));
        } else {
            if (request.getIdCliente() == null) {
                throw new IllegalArgumentException(
                        "El ID del cliente es obligatorio para reservas realizadas por personal.");
            }
            cliente = clienteRepository.findById(request.getIdCliente())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado con ID: " + request.getIdCliente()));
        }

        if (!Boolean.TRUE.equals(cliente.getUsuario().getActivo())) {
            throw new IllegalArgumentException("No se puede registrar una cita para un cliente inactivo.");
        }

        // Validar Barbero
        Barbero barbero = barberoRepository.findById(request.getIdBarbero())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Barbero no encontrado con ID: " + request.getIdBarbero()));

        if (!Boolean.TRUE.equals(barbero.getDisponible()) || !Boolean.TRUE.equals(barbero.getUsuario().getActivo())) {
            throw new IllegalArgumentException("El barbero seleccionado no se encuentra disponible o activo.");
        }

        // Validar Servicios
        if (request.getIdsServicios() == null || request.getIdsServicios().isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar al menos un servicio para la cita.");
        }

        List<Servicio> servicios = servicioRepository.findAllById(request.getIdsServicios());
        if (servicios.size() != request.getIdsServicios().size()) {
            throw new ResourceNotFoundException("Uno o más servicios seleccionados no existen.");
        }

        for (Servicio s : servicios) {
            if (!Boolean.TRUE.equals(s.getActivo())) {
                throw new IllegalArgumentException("El servicio '" + s.getNombre() + "' no se encuentra activo.");
            }
        }

        // Calcular duración y montos
        int duracionTotal = servicios.stream().mapToInt(Servicio::getDuracionMinutos).sum();
        LocalTime horaFin = request.getHoraInicio().plusMinutes(duracionTotal);

        // Validar fecha y hora no en pasado
        LocalDate hoy = LocalDate.now();
        if (request.getFecha().isBefore(hoy)) {
            throw new IllegalArgumentException("La fecha de la cita no puede ser anterior al día de hoy.");
        }
        if (request.getFecha().isEqual(hoy) && request.getHoraInicio().isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("La hora de inicio de la cita no puede ser anterior a la hora actual.");
        }

        // Validar horario laboral del barbero
        byte diaSemana = (byte) request.getFecha().getDayOfWeek().getValue();
        HorarioBarbero horarioLaboral = horarioBarberoRepository
                .findByBarberoIdBarberoAndDiaSemanaAndActivoTrue(barbero.getIdBarbero(), diaSemana)
                .orElseThrow(() -> new IllegalArgumentException("El barbero no atiende los días seleccionados."));

        if (request.getHoraInicio().isBefore(horarioLaboral.getHoraInicio())
                || horaFin.isAfter(horarioLaboral.getHoraFin())) {
            throw new IllegalArgumentException("El horario solicitado (" + request.getHoraInicio() + " - " + horaFin
                    + ") está fuera de la jornada del barbero (" + horarioLaboral.getHoraInicio() + " - "
                    + horarioLaboral.getHoraFin() + ").");
        }

        // Validar solapamiento de citas
        List<Cita> conflictos = citaRepository.findCitasSolapadas(
                barbero.getIdBarbero(), request.getFecha(), request.getHoraInicio(), horaFin, null);
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("El barbero ya tiene una cita programada que coincide con ese horario.");
        }

        // Calcular montos
        BigDecimal subtotal = servicios.stream()
                .map(Servicio::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuento = BigDecimal.ZERO;
        Promocion promocion = null;

        if (request.getCodigoPromocion() != null && !request.getCodigoPromocion().trim().isEmpty()) {
            ValidarPromocionResponse promoResp = promocionService.validarPromocion(request.getCodigoPromocion(),
                    subtotal);
            if (!promoResp.isValida()) {
                throw new IllegalArgumentException(promoResp.getMensaje());
            }
            descuento = promoResp.getMontoDescuentoCalculado();
            promocion = promocionRepository.findById(promoResp.getIdPromocion()).orElse(null);
        }

        BigDecimal montoTotal = subtotal.subtract(descuento).max(BigDecimal.ZERO);

        // Estado inicial
        EstadoCita estadoPendiente = estadoCitaRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new IllegalStateException("Estado PENDIENTE no configurado en la base de datos."));

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setBarbero(barbero);
        cita.setEstado(estadoPendiente);
        cita.setPromocion(promocion);
        cita.setCreadoPor(usuarioCreador);
        cita.setFecha(request.getFecha());
        cita.setHoraInicio(request.getHoraInicio());
        cita.setHoraFin(horaFin);
        cita.setNotas(request.getNotas());
        cita.setMontoSubtotal(subtotal);
        cita.setMontoDescuento(descuento);
        cita.setMontoTotal(montoTotal);

        Cita citaGuardada = citaRepository.save(cita);

        // Guardar detalles de servicio
        List<DetalleCita> detalles = new ArrayList<>();
        for (Servicio s : servicios) {
            DetalleCita detalle = new DetalleCita();
            detalle.setCita(citaGuardada);
            detalle.setServicio(s);
            detalle.setNombreServicio(s.getNombre());
            detalle.setPrecioUnitario(s.getPrecio());
            detalle.setCantidad(1);
            detalle.setDuracionMinutos(s.getDuracionMinutos());
            detalles.add(detalleCitaRepository.save(detalle));
        }

        // Registrar en historial
        HistorialCita historial = new HistorialCita();
        historial.setCita(citaGuardada);
        historial.setEstadoAnterior(null);
        historial.setEstadoNuevo(estadoPendiente);
        historial.setFechaNueva(citaGuardada.getFecha());
        historial.setHoraInicioNueva(citaGuardada.getHoraInicio());
        historial.setHoraFinNueva(citaGuardada.getHoraFin());
        historial.setBarberoNuevo(barbero);
        historial.setUsuarioCambio(usuarioCreador);
        historial.setObservacion("Cita registrada exitosamente.");

        historialCitaRepository.save(historial);

        return mapToResponse(citaGuardada, detalles);
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> consultarCitas(
            Integer idCliente,
            Integer idBarbero,
            String nombreEstado,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String emailUsuario,
            boolean esAdmin,
            boolean esBarbero) {

        Integer idClienteFiltro = idCliente;
        Integer idBarberoFiltro = idBarbero;

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

        List<Cita> citas = citaRepository.filtrarCitas(
                idClienteFiltro, idBarberoFiltro, nombreEstado, fechaInicio, fechaFin);

        return citas.stream()
                .map(c -> mapToResponse(c, detalleCitaRepository.findByCitaIdCita(c.getIdCita())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Integer idCita, String emailUsuario, boolean esAdmin, boolean esBarbero) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + idCita));

        if (!esAdmin) {
            if (esBarbero) {
                if (!cita.getBarbero().getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
                    throw new IllegalArgumentException("No tienes permisos para ver esta cita.");
                }
            } else {
                if (!cita.getCliente().getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
                    throw new IllegalArgumentException("No tienes permisos para ver esta cita.");
                }
            }
        }

        List<DetalleCita> detalles = detalleCitaRepository.findByCitaIdCita(cita.getIdCita());
        return mapToResponse(cita, detalles);
    }

    @Transactional
    public CitaResponse reprogramarCita(
            Integer idCita,
            ReprogramarCitaRequest request,
            String emailUsuario,
            boolean esAdmin) {

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + idCita));

        if (!esAdmin && !cita.getCliente().getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new IllegalArgumentException("No tienes permisos para reprogramar esta cita.");
        }

        String estadoActual = cita.getEstado().getNombre();
        if ("ATENDIDA".equalsIgnoreCase(estadoActual) || "CANCELADA".equalsIgnoreCase(estadoActual)) {
            throw new IllegalStateException("No se puede reprogramar una cita con estado " + estadoActual + ".");
        }

        Barbero barberoDestino = cita.getBarbero();
        if (request.getNuevoIdBarbero() != null
                && !request.getNuevoIdBarbero().equals(cita.getBarbero().getIdBarbero())) {
            barberoDestino = barberoRepository.findById(request.getNuevoIdBarbero())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Barbero no encontrado con ID: " + request.getNuevoIdBarbero()));
            if (!Boolean.TRUE.equals(barberoDestino.getDisponible())
                    || !Boolean.TRUE.equals(barberoDestino.getUsuario().getActivo())) {
                throw new IllegalArgumentException("El barbero seleccionado no se encuentra disponible.");
            }
        }

        List<DetalleCita> detalles = detalleCitaRepository.findByCitaIdCita(cita.getIdCita());
        int duracionTotal = detalles.stream().mapToInt(DetalleCita::getDuracionMinutos).sum();
        LocalTime nuevaHoraFin = request.getNuevaHoraInicio().plusMinutes(duracionTotal);

        // Validar fecha y hora no en pasado
        LocalDate hoy = LocalDate.now();
        if (request.getNuevaFecha().isBefore(hoy)) {
            throw new IllegalArgumentException("La nueva fecha no puede ser anterior al día de hoy.");
        }
        if (request.getNuevaFecha().isEqual(hoy) && request.getNuevaHoraInicio().isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("La nueva hora no puede ser anterior a la hora actual.");
        }

        // Validar horario laboral
        byte diaSemana = (byte) request.getNuevaFecha().getDayOfWeek().getValue();
        HorarioBarbero horarioLaboral = horarioBarberoRepository
                .findByBarberoIdBarberoAndDiaSemanaAndActivoTrue(barberoDestino.getIdBarbero(), diaSemana)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El barbero no atiende los días " + request.getNuevaFecha().getDayOfWeek() + "."));

        if (request.getNuevaHoraInicio().isBefore(horarioLaboral.getHoraInicio())
                || nuevaHoraFin.isAfter(horarioLaboral.getHoraFin())) {
            throw new IllegalArgumentException("El horario solicitado está fuera de la jornada del barbero.");
        }

        // Validar solapamiento (excluyendo la cita actual)
        List<Cita> conflictos = citaRepository.findCitasSolapadas(
                barberoDestino.getIdBarbero(), request.getNuevaFecha(), request.getNuevaHoraInicio(), nuevaHoraFin,
                cita.getIdCita());
        if (!conflictos.isEmpty()) {
            throw new IllegalStateException("El barbero ya tiene una cita programada en ese nuevo horario.");
        }

        Usuario usuarioCambio = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        // Registrar en historial
        HistorialCita historial = new HistorialCita();
        historial.setCita(cita);
        historial.setEstadoAnterior(cita.getEstado());
        historial.setEstadoNuevo(cita.getEstado());
        historial.setFechaAnterior(cita.getFecha());
        historial.setHoraInicioAnterior(cita.getHoraInicio());
        historial.setHoraFinAnterior(cita.getHoraFin());
        historial.setBarberoAnterior(cita.getBarbero());

        historial.setFechaNueva(request.getNuevaFecha());
        historial.setHoraInicioNueva(request.getNuevaHoraInicio());
        historial.setHoraFinNueva(nuevaHoraFin);
        historial.setBarberoNuevo(barberoDestino);
        historial.setUsuarioCambio(usuarioCambio);
        historial.setObservacion(request.getObservacion() != null ? request.getObservacion() : "Cita reprogramada.");

        historialCitaRepository.save(historial);

        // Actualizar cita
        cita.setFecha(request.getNuevaFecha());
        cita.setHoraInicio(request.getNuevaHoraInicio());
        cita.setHoraFin(nuevaHoraFin);
        cita.setBarbero(barberoDestino);

        Cita citaActualizada = citaRepository.save(cita);
        return mapToResponse(citaActualizada, detalles);
    }

    @Transactional
    public CitaResponse actualizarEstado(
            Integer idCita,
            CambiarEstadoCitaRequest request,
            String emailUsuario,
            boolean esAdmin) {

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + idCita));

        String estadoActual = cita.getEstado().getNombre();
        if ("ATENDIDA".equalsIgnoreCase(estadoActual)) {
            throw new IllegalStateException("La cita ya fue atendida y no puede cambiar de estado.");
        }
        if ("CANCELADA".equalsIgnoreCase(estadoActual)) {
            throw new IllegalStateException("La cita está cancelada y no puede modificarse.");
        }

        EstadoCita nuevoEstado = estadoCitaRepository.findByNombre(request.getNuevoEstado().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El estado '" + request.getNuevoEstado() + "' no es válido."));

        Usuario usuarioCambio = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        // Registrar en historial
        HistorialCita historial = new HistorialCita();
        historial.setCita(cita);
        historial.setEstadoAnterior(cita.getEstado());
        historial.setEstadoNuevo(nuevoEstado);
        historial.setFechaNueva(cita.getFecha());
        historial.setHoraInicioNueva(cita.getHoraInicio());
        historial.setHoraFinNueva(cita.getHoraFin());
        historial.setBarberoNuevo(cita.getBarbero());
        historial.setUsuarioCambio(usuarioCambio);
        historial.setObservacion(request.getObservacion() != null ? request.getObservacion()
                : "Cambio de estado a " + nuevoEstado.getNombre());

        historialCitaRepository.save(historial);

        cita.setEstado(nuevoEstado);
        Cita citaGuardada = citaRepository.save(cita);

        // Si pasa a ATENDIDA, calcular automáticamente la comisión del barbero
        if ("ATENDIDA".equalsIgnoreCase(nuevoEstado.getNombre())) {
            comisionService.generarComisionParaCita(citaGuardada);
        }

        List<DetalleCita> detalles = detalleCitaRepository.findByCitaIdCita(cita.getIdCita());
        return mapToResponse(citaGuardada, detalles);
    }

    @Transactional
    public CitaResponse cancelarCita(
            Integer idCita,
            CancelarCitaRequest request,
            String emailUsuario,
            boolean esAdmin) {

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + idCita));

        if (!esAdmin && !cita.getCliente().getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new IllegalArgumentException("No tienes permisos para cancelar esta cita.");
        }

        String estadoActual = cita.getEstado().getNombre();
        if ("ATENDIDA".equalsIgnoreCase(estadoActual)) {
            throw new IllegalStateException("No se puede cancelar una cita que ya fue atendida.");
        }
        if ("CANCELADA".equalsIgnoreCase(estadoActual)) {
            throw new IllegalStateException("La cita ya se encuentra cancelada.");
        }

        EstadoCita estadoCancelada = estadoCitaRepository.findByNombre("CANCELADA")
                .orElseThrow(() -> new IllegalStateException("Estado CANCELADA no configurado en la base de datos."));

        Usuario usuarioCambio = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + emailUsuario));

        // Historial
        HistorialCita historial = new HistorialCita();
        historial.setCita(cita);
        historial.setEstadoAnterior(cita.getEstado());
        historial.setEstadoNuevo(estadoCancelada);
        historial.setFechaNueva(cita.getFecha());
        historial.setHoraInicioNueva(cita.getHoraInicio());
        historial.setHoraFinNueva(cita.getHoraFin());
        historial.setBarberoNuevo(cita.getBarbero());
        historial.setUsuarioCambio(usuarioCambio);
        historial.setObservacion("Motivo de cancelación: " + request.getMotivoCancelacion());

        historialCitaRepository.save(historial);

        cita.setEstado(estadoCancelada);
        cita.setMotivoCancelacion(request.getMotivoCancelacion());

        Cita guardada = citaRepository.save(cita);
        List<DetalleCita> detalles = detalleCitaRepository.findByCitaIdCita(cita.getIdCita());
        return mapToResponse(guardada, detalles);
    }

    @Transactional(readOnly = true)
    public List<HistorialCitaResponse> obtenerHistorialCita(Integer idCita, String emailUsuario, boolean esAdmin) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + idCita));

        if (!esAdmin && !cita.getCliente().getUsuario().getEmail().equalsIgnoreCase(emailUsuario)
                && !cita.getBarbero().getUsuario().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new IllegalArgumentException("No tienes permisos para ver el historial de esta cita.");
        }

        List<HistorialCita> historial = historialCitaRepository.findByCitaIdCitaOrderByFechaCambioAsc(idCita);
        return historial.stream().map(h -> HistorialCitaResponse.builder()
                .idHistorial(h.getIdHistorial())
                .idCita(h.getCita().getIdCita())
                .estadoAnterior(h.getEstadoAnterior() != null ? h.getEstadoAnterior().getNombre() : null)
                .estadoNuevo(h.getEstadoNuevo().getNombre())
                .fechaAnterior(h.getFechaAnterior())
                .horaInicioAnterior(h.getHoraInicioAnterior())
                .horaFinAnterior(h.getHoraFinAnterior())
                .fechaNueva(h.getFechaNueva())
                .horaInicioNueva(h.getHoraInicioNueva())
                .horaFinNueva(h.getHoraFinNueva())
                .barberoAnterior(h.getBarberoAnterior() != null
                        ? h.getBarberoAnterior().getUsuario().getNombre() + " "
                                + h.getBarberoAnterior().getUsuario().getApellido()
                        : null)
                .barberoNuevo(h.getBarberoNuevo() != null
                        ? h.getBarberoNuevo().getUsuario().getNombre() + " "
                                + h.getBarberoNuevo().getUsuario().getApellido()
                        : null)
                .usuarioCambio(h.getUsuarioCambio().getNombre() + " " + h.getUsuarioCambio().getApellido())
                .observacion(h.getObservacion())
                .fechaCambio(h.getFechaCambio())
                .build()).collect(Collectors.toList());
    }

    public CitaResponse mapToResponse(Cita c, List<DetalleCita> detalles) {
        List<DetalleCitaResponse> serviciosResp = (detalles != null) ? detalles.stream()
                .map(d -> DetalleCitaResponse.builder()
                        .idDetalle(d.getIdDetalle())
                        .idServicio(d.getServicio().getIdServicio())
                        .nombreServicio(d.getNombreServicio())
                        .precioUnitario(d.getPrecioUnitario())
                        .cantidad(d.getCantidad())
                        .duracionMinutos(d.getDuracionMinutos())
                        .build())
                .collect(Collectors.toList()) : List.of();

        String nombreCliente = c.getCliente().getUsuario().getNombre() + " "
                + c.getCliente().getUsuario().getApellido();
        String nombreBarbero = c.getBarbero().getUsuario().getNombre() + " "
                + c.getBarbero().getUsuario().getApellido();
        String creador = c.getCreadoPor().getNombre() + " " + c.getCreadoPor().getApellido();

        return CitaResponse.builder()
                .idCita(c.getIdCita())
                .idCliente(c.getCliente().getIdCliente())
                .nombreCliente(nombreCliente)
                .telefonoCliente(c.getCliente().getUsuario().getTelefono())
                .emailCliente(c.getCliente().getUsuario().getEmail())
                .idBarbero(c.getBarbero().getIdBarbero())
                .nombreBarbero(nombreBarbero)
                .idEstado(c.getEstado().getIdEstado())
                .estado(c.getEstado().getNombre())
                .colorHex(c.getEstado().getColorHex())
                .fecha(c.getFecha())
                .horaInicio(c.getHoraInicio())
                .horaFin(c.getHoraFin())
                .notas(c.getNotas())
                .motivoCancelacion(c.getMotivoCancelacion())
                .montoSubtotal(c.getMontoSubtotal())
                .montoDescuento(c.getMontoDescuento())
                .montoTotal(c.getMontoTotal())
                .codigoPromocion(c.getPromocion() != null ? c.getPromocion().getCodigo() : null)
                .creadoPorNombre(creador)
                .creadoEn(c.getCreadoEn())
                .servicios(serviciosResp)
                .build();
    }
}
