package com.asbarbershop.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader =
                request.getHeader("Authorization");

        // Comprobar si existe el encabezado Bearer
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token JWT
        final String token = authorizationHeader.substring(7);

        try {

            // Extraer el correo electrónico del token
            final String username = jwtService.extractUsername(token);

            // Comprobar que todavía no exista una autenticación
            if (username != null
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                // Validar token y estado de la cuenta
                if (jwtService.isTokenValid(token, userDetails)
                        && userDetails.isEnabled()) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContext context =
                            SecurityContextHolder.createEmptyContext();

                    context.setAuthentication(authentication);

                    SecurityContextHolder.setContext(context);
                }
            }

        } catch (JwtException
                 | IllegalArgumentException
                 | UsernameNotFoundException ex) {

            // Limpiar el contexto cuando el token no sea válido
            SecurityContextHolder.clearContext();
        }

        // Continuar con los siguientes filtros
        filterChain.doFilter(request, response);
    }
}