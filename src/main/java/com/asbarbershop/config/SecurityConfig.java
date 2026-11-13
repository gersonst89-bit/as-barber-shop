package com.asbarbershop.config;

import com.asbarbershop.security.CustomUserDetailsService;
import com.asbarbershop.security.JwtAuthenticationFilter;
import com.asbarbershop.security.JwtService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Configurar proveedor de autenticacion
    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // Configurar AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    // Configurar filtros y permisos
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        userDetailsService
                );

        http
                // Configuracion CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Autenticacion mediante proveedor configurado
                .authenticationProvider(authenticationProvider)

                // Configuracion de autorizaciones
                .authorizeHttpRequests(auth -> auth

                        // Recursos estaticos publicos
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/pages/**"
                        ).permitAll()

                        // Rutas de autenticacion publicas
                        .requestMatchers("/api/auth/**").permitAll()

                        // Rutas de consulta publica (catalogo, barberos disponibles, promociones, horarios, metodos de pago)
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/barberos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/promociones/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/horarios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pagos/metodos").permitAll()

                        // Rutas administrativas
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Rutas para barberos
                        .requestMatchers("/api/barbero/**").hasAnyRole("ADMIN", "BARBERO")

                        // Rutas para clientes
                        .requestMatchers("/api/cliente/**").hasAnyRole("ADMIN", "CLIENTE")

                        // Demas rutas de la API requieren autenticacion
                        .requestMatchers("/api/**").authenticated()

                        // Resto de recursos publicos
                        .anyRequest().permitAll()
                )

                // Manejo de errores de seguridad
                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"exitoso\":false,\"mensaje\":\"No has iniciado sesión o el token es inválido.\"}");
                        })

                        .accessDeniedHandler((request, response, ex) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write("{\"exitoso\":false,\"mensaje\":\"No tienes permisos suficientes para realizar esta acción.\"}");
                        })
                )

                // Aplicacion sin sesiones tradicionales (stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Deshabilitar formularios y autenticacion HTTP Basic
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Incorporar filtro JWT
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
