package com.electromart.config;

import com.electromart.security.AuthCookies;
import com.electromart.security.CustomOidcUserService;
import com.electromart.security.JwtAuthenticationFilter;
import com.electromart.security.JwtService;
import com.electromart.security.OAuth2LoginFailureHandler;
import com.electromart.security.OAuth2LoginSuccessHandler;
import com.electromart.security.RestAccessDeniedHandler;
import com.electromart.security.RestAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   CustomOidcUserService oidcUserService,
                                                   OAuth2LoginSuccessHandler successHandler,
                                                   OAuth2LoginFailureHandler failureHandler,
                                                   RestAuthenticationEntryPoint restEntryPoint,
                                                   RestAccessDeniedHandler restAccessDeniedHandler,
                                                   AppProperties props) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // CSRF stays ON. The token cookie is readable by React, which echoes it in X-XSRF-TOKEN.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                // IF_REQUIRED: a session exists only during the Google handshake, then it is discarded.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Registered before oauth2Login so that /api/** always gets a JSON 401, never a Google redirect.
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(restEntryPoint,
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .authenticationEntryPoint(restEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/api/auth/csrf").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        // Points at a real endpoint so Spring does not generate its own HTML login page.
                        .loginPage("/oauth2/authorization/google")
                        .userInfoEndpoint(u -> u.oidcUserService(oidcUserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Clearing the cookie is what ends a stateless JWT session on the client.
                            response.addHeader(HttpHeaders.SET_COOKIE,
                                    AuthCookies.clear(props.cookie().secure()).toString());
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        }))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(AppProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        // Exact origin only. Credentialed requests cannot use a wildcard origin.
        config.setAllowedOrigins(List.of(props.frontendUrl()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
