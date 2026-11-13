package com.asbarbershop.comision.repository;

import com.asbarbershop.comision.entity.Comision;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComisionRepository extends JpaRepository<Comision, Integer> {

    @EntityGraph(attributePaths = {"cita", "barbero", "barbero.usuario"})
    Optional<Comision> findByCitaIdCita(Integer idCita);

    boolean existsByCitaIdCita(Integer idCita);

    @EntityGraph(attributePaths = {"cita", "barbero", "barbero.usuario"})
    List<Comision> findByBarberoIdBarberoOrderByCreadoEnDesc(Integer idBarbero);

    @Query("SELECT cm FROM Comision cm " +
           "JOIN FETCH cm.barbero b " +
           "JOIN FETCH b.usuario ub " +
           "JOIN FETCH cm.cita c " +
           "WHERE (:idBarbero IS NULL OR b.idBarbero = :idBarbero) " +
           "AND (:estado IS NULL OR cm.estado = :estado) " +
           "AND (:fechaInicio IS NULL OR cm.creadoEn >= :fechaInicio) " +
           "AND (:fechaFin IS NULL OR cm.creadoEn <= :fechaFin) " +
           "ORDER BY cm.creadoEn DESC")
    List<Comision> filtrarComisiones(
            @Param("idBarbero") Integer idBarbero,
            @Param("estado") String estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}

