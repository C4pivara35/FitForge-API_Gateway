package br.fitforge.gateway.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
class AccountGateway {
    private final WebClient progresso;
    private final boolean enabled;
    private final AccountScope scope;

    AccountGateway(@Qualifier("progressoWebClient") WebClient progresso,
                   @Value("${app.internal-key}") String key,
                   @Value("${app.auth-required}") boolean authenticationRequired,
                   AccountScope scope) {
        this.progresso = progresso;
        this.enabled = authenticationRequired && !key.isBlank();
        this.scope = scope;
    }

    Mono<ResponseEntity<String>> register(Credentials credentials) {
        if (!enabled) {
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Cadastro indisponível no momento."));
        }
        if (scope.isDemo(credentials.username())) {
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Esse usuário já está cadastrado."));
        }
        return progresso.post().uri("/api/internal/accounts").bodyValue(credentials).exchangeToMono(response -> {
            if (response.statusCode().value() == HttpStatus.CREATED.value()) {
                return response.releaseBody().thenReturn(ResponseEntity.status(HttpStatus.CREATED).<String>build());
            }
            HttpStatus status = switch (response.statusCode().value()) {
                case 400 -> HttpStatus.BAD_REQUEST;
                case 409 -> HttpStatus.CONFLICT;
                default -> HttpStatus.SERVICE_UNAVAILABLE;
            };
            String message = switch (status) {
                case BAD_REQUEST -> "Confira o usuário e os requisitos da senha.";
                case CONFLICT -> "Esse usuário já está cadastrado.";
                default -> "Cadastro indisponível no momento.";
            };
            return response.releaseBody().thenReturn(ResponseEntity.status(status).body(message));
        }).timeout(Duration.ofSeconds(10)).onErrorReturn(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Cadastro indisponível no momento."));
    }

    Mono<Boolean> authenticate(String username, String password) {
        if (!enabled) return Mono.just(false);
        return progresso.post().uri("/api/internal/accounts/authenticate").bodyValue(new Credentials(username, password))
                .retrieve().toBodilessEntity().thenReturn(true)
                .onErrorReturn(WebClientResponseException.Unauthorized.class, false)
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(cause -> new AuthenticationServiceException("Serviço de contas indisponível.", cause));
    }

    record Credentials(String username, String password) {}
}
