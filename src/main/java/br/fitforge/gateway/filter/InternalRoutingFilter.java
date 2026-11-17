package br.fitforge.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class InternalRoutingFilter implements GlobalFilter, Ordered {
    private static final String INTERNAL_HEADER = "X-FitForge-Internal-Key";
    private final String key;

    public InternalRoutingFilter(@Value("${app.internal-key}") String key) {
        this.key = key;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest().mutate().headers(headers -> {
            headers.remove(HttpHeaders.AUTHORIZATION);
            headers.remove(INTERNAL_HEADER);
            if (!key.isBlank()) {
                headers.set(INTERNAL_HEADER, key);
            }
        }).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
