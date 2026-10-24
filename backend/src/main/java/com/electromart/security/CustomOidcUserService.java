package com.electromart.security;

import com.electromart.entity.User;
import com.electromart.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Runs during Google login. Spring's default OidcUserService first validates Google's ID token
 * (signature, issuer, audience, nonce). Then we find or create the local user.
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserService userService;

    public CustomOidcUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Google/OIDC authentication: exchanges the code for tokens and validates the ID token.
        OidcUser oidcUser = super.loadUser(userRequest);

        // Email is only trusted when Google has verified it.
        if (!Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_verified"),
                    "Google account email is not verified");
        }

        // "sub" is Google's stable identifier for this account. Email is not used as the key.
        User user = userService.findOrCreateGoogleUser(
                oidcUser.getSubject(), oidcUser.getEmail(), oidcUser.getFullName(), oidcUser.getPicture());

        // Authority comes from OUR database role (USER by default), not from Google.
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub");
    }
}
