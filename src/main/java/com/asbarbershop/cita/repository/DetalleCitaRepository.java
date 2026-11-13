package com.asbarbershop.cita.repository;

import com.asbarbershop.cita.entity.DetalleCita;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCitaRepository extends JpaRepository<DetalleCita, Integer> {

    @EntityGraph(attributePaths = {"servicio"})
    List<DetalleCita> findByCitaIdCita(Integer idCita);

    void deleteByCitaIdCita(Integer idCita);
}

