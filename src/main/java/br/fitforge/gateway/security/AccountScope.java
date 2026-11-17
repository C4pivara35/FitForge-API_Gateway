package br.fitforge.gateway.security;

import java.security.Principal;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccountScope {
    public static final String HEADER = "X-FitForge-Owner";
    private final String demoUsername;

    public AccountScope(@Value("${app.username}") String demoUsername) {
        this.demoUsername = demoUsername;
    }

    boolean isDemo(String username) {
        return demoUsername.equalsIgnoreCase(username);
    }

    public Optional<String> owner(Principal principal) {
        if (!(principal instanceof Authentication authentication) || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return Optional.empty();
        String username = authentication.getName();
        return isDemo(username) ? Optional.empty() : Optional.of(username.toLowerCase(Locale.ROOT));
    }
}
