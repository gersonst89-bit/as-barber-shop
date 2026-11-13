package com.asbarbershop.promocion.repository;

import com.asbarbershop.promocion.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    Optional<Promocion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    @Query("SELECT p FROM Promocion p WHERE p.activo = true AND :fecha BETWEEN p.fechaInicio AND p.fechaFin")
    List<Promocion> findVigentes(@Param("fecha") LocalDate fecha);
}

