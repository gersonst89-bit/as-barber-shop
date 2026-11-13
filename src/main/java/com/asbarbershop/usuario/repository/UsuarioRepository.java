package com.asbarbershop.usuario.repository;

import com.asbarbershop.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByTelefono(String telefono);
}