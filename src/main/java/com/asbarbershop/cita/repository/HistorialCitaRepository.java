package com.asbarbershop.cita.repository;

import com.asbarbershop.cita.entity.HistorialCita;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCitaRepository extends JpaRepository<HistorialCita, Integer> {

    @EntityGraph(attributePaths = {
            "estadoAnterior",
            "estadoNuevo",
            "barberoAnterior",
            "barberoAnterior.usuario",
            "barberoNuevo",
            "barberoNuevo.usuario",
            "usuarioCambio"
    })
    List<HistorialCita> findByCitaIdCitaOrderByFechaCambioAsc(Integer idCita);
}

