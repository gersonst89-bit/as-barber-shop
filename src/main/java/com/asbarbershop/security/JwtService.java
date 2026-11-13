package com.asbarbershop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Generar token JWT
    public String generateToken(UserDetails userDetails) {

        Date fechaActual = new Date();

        Date fechaExpiracion = new Date(
                fechaActual.getTime() + expiration
        );

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(fechaActual)
                .expiration(fechaExpiracion)
                .signWith(signingKey)
                .compact();
    }

    // Extraer correo del token
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Validar token
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        try {
            String username = extractUsername(token);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);

        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // Comprobar expiración
    private boolean isTokenExpired(String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // Obtener información del token
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}