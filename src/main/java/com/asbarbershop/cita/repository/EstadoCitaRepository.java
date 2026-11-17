package com.asbarbershop.cita.repository;

import com.asbarbershop.cita.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoCitaRepository extends JpaRepository<EstadoCita, Integer> {

    Optional<EstadoCita> findByNombre(String nombre);
}
