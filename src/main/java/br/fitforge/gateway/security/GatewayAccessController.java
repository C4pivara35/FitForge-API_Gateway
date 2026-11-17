package br.fitforge.gateway.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayAccessController {
    @GetMapping("/api/auth/check")
    ResponseEntity<Void> check() {
        return ResponseEntity.noContent().build();
    }
}
