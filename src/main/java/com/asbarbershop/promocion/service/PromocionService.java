package com.asbarbershop.promocion.service;

import com.asbarbershop.common.exception.ResourceNotFoundException;
import com.asbarbershop.promocion.dto.PromocionRequest;
import com.asbarbershop.promocion.dto.PromocionResponse;
import com.asbarbershop.promocion.dto.ValidarPromocionResponse;
import com.asbarbershop.promocion.entity.Promocion;
import com.asbarbershop.promocion.repository.PromocionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromocionService {

    private final PromocionRepository promocionRepository;

    @Transactional(readOnly = true)
    public List<PromocionResponse> listarVigentes() {
        return promocionRepository.findVigentes(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromocionResponse> listarTodas() {
        return promocionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromocionResponse obtenerPorId(Integer id) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));
        return mapToResponse(promocion);
    }

    @Transactional(readOnly = true)
    public ValidarPromocionResponse validarPromocion(String codigo, BigDecimal montoSubtotal) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return ValidarPromocionResponse.builder()
                    .valida(false)
                    .mensaje("El código promocional no puede estar vacío.")
                    .montoDescuentoCalculado(BigDecimal.ZERO)
                    .montoFinal(montoSubtotal != null ? montoSubtotal : BigDecimal.ZERO)
                    .build();
        }

        Promocion promocion = promocionRepository.findByCodigo(codigo.trim().toUpperCase())
                .orElse(null);

        if (promocion == null) {
            return ValidarPromocionResponse.builder()
                    .valida(false)
                    .mensaje("El cupón '" + codigo + "' no existe.")
                    .montoDescuentoCalculado(BigDecimal.ZERO)
                    .montoFinal(montoSubtotal)
                    .build();
        }

        if (!Boolean.TRUE.equals(promocion.getActivo())) {
            return ValidarPromocionResponse.builder()
                    .valida(false)
                    .mensaje("El cupón '" + codigo + "' se encuentra inactivo.")
                    .montoDescuentoCalculado(BigDecimal.ZERO)
                    .montoFinal(montoSubtotal)
                    .build();
        }

        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(promocion.getFechaInicio()) || hoy.isAfter(promocion.getFechaFin())) {
            return ValidarPromocionResponse.builder()
                    .valida(false)
                    .mensaje("El cupón '" + codigo + "' no se encuentra dentro de su periodo de vigencia.")
                    .montoDescuentoCalculado(BigDecimal.ZERO)
                    .montoFinal(montoSubtotal)
                    .build();
        }

        if (montoSubtotal == null || montoSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidarPromocionResponse.builder()
                    .valida(true)
                    .mensaje("Cupón válido.")
                    .idPromocion(promocion.getIdPromocion())
                    .codigo(promocion.getCodigo())
                    .descripcion(promocion.getDescripcion())
                    .tipoDescuento(promocion.getTipoDescuento())
                    .valorDescuento(promocion.getValorDescuento())
                    .montoDescuentoCalculado(BigDecimal.ZERO)
                    .montoFinal(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal descuento;
        if ("PORCENTAJE".equalsIgnoreCase(promocion.getTipoDescuento())) {
            descuento = montoSubtotal.multiply(promocion.getValorDescuento())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            descuento = promocion.getValorDescuento().min(montoSubtotal);
        }

        BigDecimal montoFinal = montoSubtotal.subtract(descuento).max(BigDecimal.ZERO);

        return ValidarPromocionResponse.builder()
                .valida(true)
                .mensaje("¡Cupón aplicado exitosamente!")
                .idPromocion(promocion.getIdPromocion())
                .codigo(promocion.getCodigo())
                .descripcion(promocion.getDescripcion())
                .tipoDescuento(promocion.getTipoDescuento())
                .valorDescuento(promocion.getValorDescuento())
                .montoDescuentoCalculado(descuento)
                .montoFinal(montoFinal)
                .build();
    }

    @Transactional
    public PromocionResponse crearPromocion(PromocionRequest request) {
        String codigoLimpio = request.getCodigo().trim().toUpperCase();

        if (promocionRepository.existsByCodigo(codigoLimpio)) {
            throw new IllegalArgumentException("Ya existe una promoción con el código: " + codigoLimpio);
        }

        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if ("PORCENTAJE".equalsIgnoreCase(request.getTipoDescuento())
                && request.getValorDescuento().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje de descuento no puede ser mayor al 100%.");
        }

        Promocion promocion = new Promocion();
        promocion.setCodigo(codigoLimpio);
        promocion.setDescripcion(request.getDescripcion());
        promocion.setTipoDescuento(request.getTipoDescuento().toUpperCase());
        promocion.setValorDescuento(request.getValorDescuento());
        promocion.setFechaInicio(request.getFechaInicio());
        promocion.setFechaFin(request.getFechaFin());
        promocion.setActivo(request.getActivo() != null ? request.getActivo() : true);

        Promocion guardada = promocionRepository.save(promocion);
        return mapToResponse(guardada);
    }

    @Transactional
    public PromocionResponse actualizarPromocion(Integer id, PromocionRequest request) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));

        String codigoLimpio = request.getCodigo().trim().toUpperCase();
        if (!promocion.getCodigo().equalsIgnoreCase(codigoLimpio)
                && promocionRepository.existsByCodigo(codigoLimpio)) {
            throw new IllegalArgumentException("Ya existe una promoción con el código: " + codigoLimpio);
        }

        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if ("PORCENTAJE".equalsIgnoreCase(request.getTipoDescuento())
                && request.getValorDescuento().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje de descuento no puede ser mayor al 100%.");
        }

        promocion.setCodigo(codigoLimpio);
        promocion.setDescripcion(request.getDescripcion());
        promocion.setTipoDescuento(request.getTipoDescuento().toUpperCase());
        promocion.setValorDescuento(request.getValorDescuento());
        promocion.setFechaInicio(request.getFechaInicio());
        promocion.setFechaFin(request.getFechaFin());
        if (request.getActivo() != null) {
            promocion.setActivo(request.getActivo());
        }

        Promocion guardada = promocionRepository.save(promocion);
        return mapToResponse(guardada);
    }

    @Transactional
    public void cambiarEstado(Integer id, boolean activo) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));

        promocion.setActivo(activo);
        promocionRepository.save(promocion);
    }

    @Transactional
    public void eliminarPromocion(Integer id) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promoción no encontrada con ID: " + id));

        try {
            promocionRepository.delete(promocion);
        } catch (Exception ex) {
            throw new IllegalStateException("No se puede eliminar la promoción porque ya está vinculada a citas existentes. Utiliza la desactivación en su lugar.");
        }
    }

    public PromocionResponse mapToResponse(Promocion p) {
        LocalDate hoy = LocalDate.now();
        boolean esVigente = Boolean.TRUE.equals(p.getActivo())
                && !hoy.isBefore(p.getFechaInicio())
                && !hoy.isAfter(p.getFechaFin());

        return PromocionResponse.builder()
                .idPromocion(p.getIdPromocion())
                .codigo(p.getCodigo())
                .descripcion(p.getDescripcion())
                .tipoDescuento(p.getTipoDescuento())
                .valorDescuento(p.getValorDescuento())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activo(p.getActivo())
                .vigente(esVigente)
                .build();
    }
}

