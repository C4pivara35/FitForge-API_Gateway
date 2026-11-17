package br.fitforge.gateway;

import br.fitforge.gateway.bff.service.BffService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class BffServiceUnitTests {

    @Test
    void agregaFichaSessaoEEstatisticasDeDuasOrigens() {
        WebClient treinos = WebClient.builder().baseUrl("http://treinos.invalid")
                .exchangeFunction(request -> {
                    assertThat(request.headers().getFirst("X-Correlation-Id")).isEqualTo("teste");
                    return json("""
                            [{"id":"a","name":"Treino A","description":"","days":["Seg"],"exercises":[]}]
                            """);
                }).build();
        WebClient progresso = WebClient.builder().baseUrl("http://progresso.invalid")
                .exchangeFunction(request -> request.url().getPath().endsWith("/estatisticas")
                        ? json("""
                                {"totalSessoes":1,"minutosTotais":44,"sessoesNaSemana":1,"minutosNaSemana":44,"semana":[]}
                                """)
                        : json("""
                                [{"id":"s","workoutId":"a","workoutName":"Treino A","date":"2026-01-05","duration":44,"effort":7,"notes":""}]
                                """)).build();
        Clock clock = Clock.fixed(Instant.parse("2026-01-05T13:00:00Z"), ZoneOffset.UTC);
        BffService service = new BffService(treinos, progresso, 2, "http://treinos.invalid", "http://progresso.invalid", clock);

        StepVerifier.create(service.dashboard("teste")).assertNext(result -> {
            assertThat(result.totalFichas()).isEqualTo(1);
            assertThat(result.minutosNaSemana()).isEqualTo(44);
            assertThat(result.treinosDeHoje()).hasSize(1);
            assertThat(result.ultimasSessoes()).hasSize(1);
            assertThat(result.statusMicrosservicos()).containsValues("ONLINE");
        }).verifyComplete();
    }

    private static Mono<ClientResponse> json(String value) {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, "application/json").body(value).build());
    }
}
