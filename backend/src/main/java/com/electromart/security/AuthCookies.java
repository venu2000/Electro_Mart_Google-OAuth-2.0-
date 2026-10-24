package com.electromart.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

/** Builds the cookie that carries OUR application JWT to the browser. */
public final class AuthCookies {

    public static final String NAME = "ELECTROMART_JWT";

    private AuthCookies() {
    }

    /** HttpOnly: JavaScript cannot read it, which limits damage from XSS. */
    public static ResponseCookie issue(String jwt, Duration maxAge, boolean secure) {
        return ResponseCookie.from(NAME, jwt)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax") // Lax blocks the cookie on cross-site POSTs (CSRF defence in depth)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public static ResponseCookie clear(boolean secure) {
        return ResponseCookie.from(NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }
}
