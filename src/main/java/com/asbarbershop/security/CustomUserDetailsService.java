package com.asbarbershop.security;

import com.asbarbershop.usuario.entity.Usuario;
import com.asbarbershop.usuario.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No se encontró un usuario con el correo: " + email
                        )
                );

        return User.withUsername(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().getNombre())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}