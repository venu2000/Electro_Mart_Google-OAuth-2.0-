package com.electromart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Typed view of the "app.*" block in application.yml. All values come from environment variables. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String frontendUrl, Jwt jwt, AuthCookie cookie) {

    public record Jwt(String secret, Duration expiration, String issuer) {
    }

    public record AuthCookie(boolean secure) {
    }
}
