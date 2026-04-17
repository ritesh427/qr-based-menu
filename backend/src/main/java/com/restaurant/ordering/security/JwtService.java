package com.restaurant.ordering.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationInSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-seconds}") long expirationInSeconds) {
        this.secretKey = secret.length() >= 32
                ? Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))
                : Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationInSeconds = expirationInSeconds;
    }

    public String generateToken(String username, Long restaurantId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("restaurantId", restaurantId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationInSeconds)))
                .signWith(secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
}
