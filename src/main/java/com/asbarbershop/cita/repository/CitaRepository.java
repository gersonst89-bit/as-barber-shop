package com.asbarbershop.cita.repository;

import com.asbarbershop.cita.entity.Cita;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    @EntityGraph(attributePaths = { "cliente", "cliente.usuario", "barbero", "barbero.usuario", "estado", "promocion" })
    Optional<Cita> findById(Integer id);

    @EntityGraph(attributePaths = { "cliente", "cliente.usuario", "barbero", "barbero.usuario", "estado", "promocion" })
    List<Cita> findByClienteIdClienteOrderByFechaDescHoraInicioDesc(Integer idCliente);

    @EntityGraph(attributePaths = { "cliente", "cliente.usuario", "barbero", "barbero.usuario", "estado", "promocion" })
    List<Cita> findByBarberoIdBarberoOrderByFechaDescHoraInicioDesc(Integer idBarbero);

    @EntityGraph(attributePaths = { "cliente", "cliente.usuario", "barbero", "barbero.usuario", "estado", "promocion" })
    List<Cita> findByBarberoIdBarberoAndFechaOrderByHoraInicioAsc(Integer idBarbero, LocalDate fecha);

    // Consulta para validar si el barbero ya tiene otra cita en el mismo rango
    // horario (excluyendo citas canceladas)
    @Query("SELECT c FROM Cita c WHERE c.barbero.idBarbero = :idBarbero " +
            "AND c.fecha = :fecha " +
            "AND c.estado.nombre <> 'CANCELADA' " +
            "AND (:excluirIdCita IS NULL OR c.idCita <> :excluirIdCita) " +
            "AND (:horaInicio < c.horaFin AND :horaFin > c.horaInicio)")
    List<Cita> findCitasSolapadas(
            @Param("idBarbero") Integer idBarbero,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excluirIdCita") Integer excluirIdCita);

    // Consulta para filtros dinámicos en reportes y vistas administrativas
    @Query("SELECT c FROM Cita c " +
            "JOIN FETCH c.cliente cl " +
            "JOIN FETCH cl.usuario ucl " +
            "JOIN FETCH c.barbero b " +
            "JOIN FETCH b.usuario ub " +
            "JOIN FETCH c.estado e " +
            "LEFT JOIN FETCH c.promocion p " +
            "WHERE (:idCliente IS NULL OR c.cliente.idCliente = :idCliente) " +
            "AND (:idBarbero IS NULL OR c.barbero.idBarbero = :idBarbero) " +
            "AND (:nombreEstado IS NULL OR e.nombre = :nombreEstado) " +
            "AND (:fechaInicio IS NULL OR c.fecha >= :fechaInicio) " +
            "AND (:fechaFin IS NULL OR c.fecha <= :fechaFin) " +
            "ORDER BY c.fecha DESC, c.horaInicio DESC")
    List<Cita> filtrarCitas(
            @Param("idCliente") Integer idCliente,
            @Param("idBarbero") Integer idBarbero,
            @Param("nombreEstado") String nombreEstado,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
