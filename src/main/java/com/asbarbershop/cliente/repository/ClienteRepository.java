package com.asbarbershop.cliente.repository;

import com.asbarbershop.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Cliente> findByUsuarioEmail(String email);

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Cliente> findById(Integer id);
}

