package br.fitforge.gateway.security;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
public class GatewaySecurity {

    @Bean
    SecurityWebFilterChain security(ServerHttpSecurity http,
                                    UrlBasedCorsConfigurationSource cors,
                                    @Value("${app.auth-required}") boolean required,
                                    @Value("${app.username}") String username,
                                    @Value("${app.password}") String password) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .cors(spec -> spec.configurationSource(cors));
        if (required) {
            if (!Pattern.matches("[a-zA-Z0-9_.@-]+", username) || password.isBlank()) {
                throw new IllegalStateException("Configure FITFORGE_USERNAME e FITFORGE_PASSWORD no Gateway.");
            }
            var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            var user = User.withUsername(username).password(encoder.encode(password)).roles("FITFORGE").build();
            http.authenticationManager(new org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager(
                    new MapReactiveUserDetailsService(user)));
            http.httpBasic(Customizer.withDefaults());
        } else {
            http.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
        }
        http.authorizeExchange(routes -> {
            routes.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            if (required) {
                routes.pathMatchers("/api/**").hasRole("FITFORGE");
                routes.pathMatchers("/", "/index.html", "/actuator/health", "/swagger-ui.html", "/swagger-ui/**",
                        "/webjars/**", "/v3/api-docs", "/v3/api-docs/**").permitAll();
                routes.anyExchange().denyAll();
            } else {
                routes.anyExchange().permitAll();
            }
        });
        return http.build();
    }

    @Bean
    UrlBasedCorsConfigurationSource cors(@Value("${app.cors-origins}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id"));
        config.setExposedHeaders(List.of("X-Correlation-Id", "X-Gateway-Route", "Location"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
