package com.electromart.security;

import com.electromart.config.AppProperties;
import com.electromart.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Creates and validates OUR application JWT (HS256).
 * This is NOT Google's ID token. Google's tokens stay on the server; the browser only gets this JWT.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final String issuer;
    private final Duration expiration;

    public JwtService(AppProperties props) {
        byte[] secretBytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = props.jwt().issuer();
        this.expiration = props.jwt().expiration();
    }

    /** JWT GENERATION: called once, after Google login succeeds and the local user is loaded. */
    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId())) // sub = OUR user id, not Google's sub
                .issuer(issuer)
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * JWT VALIDATION: verifies the signature, issuer and expiration.
     * Throws ExpiredJwtException or JwtException when the token is not acceptable.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Duration getExpiration() {
        return expiration;
    }
}
