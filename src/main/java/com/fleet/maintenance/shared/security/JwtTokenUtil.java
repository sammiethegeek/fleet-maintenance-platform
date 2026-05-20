package com.fleet.maintenance.shared.security;

import com.fleet.maintenance.shared.dto.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    private static final long MAX_EXPIRATION_MS = 3_600_000;

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = Math.min(expirationMs, MAX_EXPIRATION_MS);
    }

    public String generateToken(String id, String name, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(id)
                .claim("id", id)
                .claim("name", name)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public UserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String id = claims.get("id", String.class);
        String name = claims.get("name", String.class);
        Role role = Role.valueOf(claims.get("role", String.class));
        return new UserPrincipal(id, name, role);
    }
}
