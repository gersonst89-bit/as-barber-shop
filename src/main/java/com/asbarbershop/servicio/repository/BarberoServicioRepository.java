package com.asbarbershop.servicio.repository;

import com.asbarbershop.servicio.entity.BarberoServicio;
import com.asbarbershop.servicio.entity.BarberoServicioId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarberoServicioRepository extends JpaRepository<BarberoServicio, BarberoServicioId> {

    @EntityGraph(attributePaths = {"servicio"})
    List<BarberoServicio> findByBarberoIdBarbero(Integer idBarbero);

    @EntityGraph(attributePaths = {"barbero", "barbero.usuario"})
    List<BarberoServicio> findByServicioIdServicio(Integer idServicio);

    boolean existsByIdIdBarberoAndIdIdServicio(Integer idBarbero, Integer idServicio);

    void deleteByIdIdBarberoAndIdIdServicio(Integer idBarbero, Integer idServicio);
}

