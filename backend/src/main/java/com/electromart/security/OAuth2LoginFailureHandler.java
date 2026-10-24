package com.electromart.security;

import com.electromart.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Sends the user back to the React login page with a generic error code. Details stay in the log. */
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

    private final AppProperties props;

    public OAuth2LoginFailureHandler(AppProperties props) {
        this.props = props;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("Google OAuth2 login failed: {}", exception.getMessage());
        response.sendRedirect(props.frontendUrl() + "/login?error=oauth_failed");
    }
}
