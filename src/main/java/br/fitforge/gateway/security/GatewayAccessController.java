package br.fitforge.gateway.security;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayAccessController {
    private final AccountGateway accounts;

    public GatewayAccessController(AccountGateway accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/api/auth/register")
    Mono<ResponseEntity<Map<String, String>>> register(@RequestBody AccountGateway.Credentials credentials) {
        return accounts.register(credentials).map(result -> result.getBody() == null
                ? ResponseEntity.status(result.getStatusCode()).build()
                : ResponseEntity.status(result.getStatusCode()).body(Map.of("message", result.getBody())));
    }

    @GetMapping("/api/auth/check")
    ResponseEntity<Void> check() {
        return ResponseEntity.noContent().build();
    }
}
