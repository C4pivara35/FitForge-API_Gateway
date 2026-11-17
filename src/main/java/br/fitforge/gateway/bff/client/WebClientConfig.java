package br.fitforge.gateway.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Clock;
import java.time.ZoneId;

/** Clientes HTTP reativos usados pelo BFF + relógio no fuso do usuário. */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient treinosWebClient(WebClient.Builder builder, @Value("${services.treinos.url}") String url,
                                      @Value("${app.internal-key}") String internalKey) {
        return client(builder, url, internalKey);
    }

    @Bean
    public WebClient progressoWebClient(WebClient.Builder builder, @Value("${services.progresso.url}") String url,
                                        @Value("${app.internal-key}") String internalKey) {
        return client(builder, url, internalKey);
    }

    private WebClient client(WebClient.Builder builder, String url, String internalKey) {
        WebClient.Builder client = builder.clone().baseUrl(url);
        if (!internalKey.isBlank()) {
            client.defaultHeader("X-FitForge-Internal-Key", internalKey);
        }
        return client.build();
    }

    @Bean
    public Clock clock(@Value("${app.timezone:America/Sao_Paulo}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
