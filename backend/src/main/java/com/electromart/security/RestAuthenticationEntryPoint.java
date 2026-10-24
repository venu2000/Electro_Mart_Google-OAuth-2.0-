package com.electromart.security;

import com.electromart.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Returns 401 for unauthenticated or invalid-token requests. It never redirects an API call to Google.
 * The error code tells the React app whether the session expired or the token was invalid.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object reason = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
        String code = reason != null ? reason.toString() : "AUTHENTICATION_REQUIRED";
        String message = switch (code) {
            case JwtAuthenticationFilter.ERROR_TOKEN_EXPIRED -> "Your session has expired. Please sign in again.";
            case JwtAuthenticationFilter.ERROR_TOKEN_INVALID -> "Invalid authentication token. Please sign in again.";
            default -> "Authentication is required to access this resource.";
        };

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new ApiError(401, code, message, Instant.now()));
    }
}
