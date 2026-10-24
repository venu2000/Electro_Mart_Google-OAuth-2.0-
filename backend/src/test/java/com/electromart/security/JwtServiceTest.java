package com.electromart.security;

import com.electromart.config.AppProperties;
import com.electromart.entity.AuthProvider;
import com.electromart.entity.Role;
import com.electromart.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-that-is-at-least-32-bytes-long";

    private JwtService service(String secret, Duration expiration) {
        AppProperties props = new AppProperties("http://localhost:5173",
                new AppProperties.Jwt(secret, expiration, "electromart"),
                new AppProperties.AuthCookie(false));
        return new JwtService(props);
    }

    private User user() {
        User user = new User("google-sub-123", "learner@example.com", "Learner", null, AuthProvider.GOOGLE, Role.USER);
        ReflectionTestUtils.setField(user, "id", 42L);
        return user;
    }

    @Test
    void tokenContainsOurApplicationClaims() {
        JwtService jwt = service(SECRET, Duration.ofHours(1));

        var claims = jwt.parseToken(jwt.generateToken(user()));

        // sub is OUR user id, never Google's sub
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("email", String.class)).isEqualTo("learner@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = service(SECRET, Duration.ofSeconds(-30));
        String token = jwt.generateToken(user());

        assertThatThrownBy(() -> jwt.parseToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = service(SECRET, Duration.ofHours(1));
        String token = jwt.generateToken(user());
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> jwt.parseToken(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = service(SECRET, Duration.ofHours(1)).generateToken(user());
        JwtService other = service("another-secret-that-is-also-at-least-32-bytes", Duration.ofHours(1));

        assertThatThrownBy(() -> other.parseToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void shortSecretIsRejectedAtStartup() {
        assertThatThrownBy(() -> service("too-short", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
