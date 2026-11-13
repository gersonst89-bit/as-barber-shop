package com.asbarbershop.pago.repository;

import com.asbarbershop.pago.entity.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    @EntityGraph(attributePaths = {"cita", "cita.cliente", "cita.cliente.usuario", "metodoPago", "registradoPor"})
    List<Pago> findByCitaIdCita(Integer idCita);

    @Query("SELECT p FROM Pago p " +
           "JOIN FETCH p.cita c " +
           "JOIN FETCH c.cliente cl " +
           "JOIN FETCH cl.usuario ucl " +
           "JOIN FETCH c.barbero b " +
           "JOIN FETCH b.usuario ub " +
           "JOIN FETCH p.metodoPago m " +
           "JOIN FETCH p.registradoPor reg " +
           "WHERE (:idCita IS NULL OR c.idCita = :idCita) " +
           "AND (:idCliente IS NULL OR cl.idCliente = :idCliente) " +
           "AND (:idBarbero IS NULL OR b.idBarbero = :idBarbero) " +
           "AND (:idMetodo IS NULL OR m.idMetodo = :idMetodo) " +
           "AND (:fechaInicio IS NULL OR p.fechaPago >= :fechaInicio) " +
           "AND (:fechaFin IS NULL OR p.fechaPago <= :fechaFin) " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> filtrarPagos(
            @Param("idCita") Integer idCita,
            @Param("idCliente") Integer idCliente,
            @Param("idBarbero") Integer idBarbero,
            @Param("idMetodo") Integer idMetodo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}

