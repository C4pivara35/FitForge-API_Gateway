package br.fitforge.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "app.auth-required=true", "app.username=teste", "app.password=senha-de-teste",
        "app.cors-origins=https://fitforge.example"
})
class GatewaySecurityTests {
    @Autowired WebTestClient client;

    @Test
    void protegeApiEDocumentaOsProxies() {
        client.get().uri("/index.html").exchange().expectStatus().isOk();
        client.get().uri("/api/auth/check").exchange().expectStatus().isUnauthorized();
        client.get().uri("/api/auth/check").headers(h -> h.setBasicAuth("teste", "senha-de-teste"))
                .exchange().expectStatus().isNoContent();
        client.get().uri("/v3/api-docs").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.paths['/api/bff/dashboard'].get").exists()
                .jsonPath("$.paths['/api/treinos'].post").exists();
    }

    @Test
    void permitePreflightDaOrigemConfigurada() {
        client.options().uri("/api/bff/dashboard")
                .header(HttpHeaders.ORIGIN, "https://fitforge.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization")
                .exchange().expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://fitforge.example");

        client.options().uri("/api/bff/dashboard")
                .header(HttpHeaders.ORIGIN, "https://outra.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange().expectStatus().isForbidden();
    }
}
