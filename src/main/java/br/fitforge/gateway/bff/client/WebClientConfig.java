package br.fitforge.gateway.bff.client;

import br.fitforge.gateway.security.AccountScope;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
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
                                      @Value("${app.internal-key}") String internalKey, AccountScope scope) {
        return client(builder, url, internalKey, scope);
    }

    @Bean
    public WebClient progressoWebClient(WebClient.Builder builder, @Value("${services.progresso.url}") String url,
                                        @Value("${app.internal-key}") String internalKey, AccountScope scope) {
        return client(builder, url, internalKey, scope);
    }

    private WebClient client(WebClient.Builder builder, String url, String internalKey, AccountScope scope) {
        WebClient.Builder client = builder.clone().baseUrl(url).filter((request, next) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(context -> scope.owner(context.getAuthentication()))
                        .map(owner -> owner.map(value -> ClientRequest.from(request)
                                .header(AccountScope.HEADER, value).build()).orElse(request))
                        .defaultIfEmpty(request)
                        .flatMap(next::exchange));
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
