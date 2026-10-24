package com.electromart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs on every request. Reads OUR JWT from the HttpOnly cookie (or an Authorization: Bearer header),
 * validates it, and places an Authentication in the SecurityContext.
 * Invalid or expired tokens do NOT authenticate; the entry point then returns 401.
 * It is deliberately not a @Component, so Spring Boot does not also register it as a servlet filter.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR_ATTRIBUTE = "electromart.auth.error";
    public static final String ERROR_TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_TOKEN_INVALID = "TOKEN_INVALID";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            try {
                // Signature and expiration are checked here; any failure throws a JwtException.
                Claims claims = jwtService.parseToken(token);

                // Claims are read from OUR token, not from Google's.
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                if (userId == null || role == null) {
                    throw new JwtException("Token is missing required claims");
                }

                // Authentication goes into the SecurityContext. Controllers read it via Authentication.
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException ex) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, ERROR_TOKEN_EXPIRED);
            } catch (JwtException | IllegalArgumentException ex) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, ERROR_TOKEN_INVALID);
            }
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (AuthCookies.NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
