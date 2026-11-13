package com.asbarbershop.servicio.repository;

import com.asbarbershop.servicio.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    List<Servicio> findByActivoTrue();

    boolean existsByNombre(String nombre);
}

