package com.asbarbershop.barbero.repository;

import com.asbarbershop.barbero.entity.Barbero;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BarberoRepository extends JpaRepository<Barbero, Integer> {

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Barbero> findByUsuarioEmail(String email);

    @EntityGraph(attributePaths = {"usuario"})
    List<Barbero> findByDisponibleTrueAndUsuarioActivoTrue();

    @EntityGraph(attributePaths = {"usuario"})
    List<Barbero> findAll();

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Barbero> findById(Integer id);
}

