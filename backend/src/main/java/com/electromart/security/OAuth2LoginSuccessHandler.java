package com.electromart.security;

import com.electromart.config.AppProperties;
import com.electromart.entity.User;
import com.electromart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Runs after Google login succeeds. Issues OUR application JWT as an HttpOnly cookie,
 * drops the temporary OAuth session, and redirects the browser back to React.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final AppProperties props;

    public OAuth2LoginSuccessHandler(UserService userService, JwtService jwtService, AppProperties props) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.props = props;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        User user = userService.findByGoogleSubject(oidcUser.getSubject());

        // JWT GENERATION: the Google ID token is NOT forwarded to the browser; we issue our own JWT.
        String appJwt = jwtService.generateToken(user);
        response.addHeader(HttpHeaders.SET_COOKIE,
                AuthCookies.issue(appJwt, jwtService.getExpiration(), props.cookie().secure()).toString());

        // The OAuth handshake session is no longer needed. API auth is JWT-only from here on.
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(props.frontendUrl() + "/products");
    }
}
