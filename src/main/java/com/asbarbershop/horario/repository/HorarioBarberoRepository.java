package com.asbarbershop.horario.repository;

import com.asbarbershop.horario.entity.HorarioBarbero;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HorarioBarberoRepository extends JpaRepository<HorarioBarbero, Integer> {

    @EntityGraph(attributePaths = {"barbero", "barbero.usuario"})
    List<HorarioBarbero> findByBarberoIdBarberoOrderByDiaSemanaAscHoraInicioAsc(Integer idBarbero);

    @EntityGraph(attributePaths = {"barbero", "barbero.usuario"})
    List<HorarioBarbero> findByBarberoIdBarberoAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(Integer idBarbero);

    @EntityGraph(attributePaths = {"barbero", "barbero.usuario"})
    Optional<HorarioBarbero> findByBarberoIdBarberoAndDiaSemanaAndActivoTrue(Integer idBarbero, Byte diaSemana);
}

