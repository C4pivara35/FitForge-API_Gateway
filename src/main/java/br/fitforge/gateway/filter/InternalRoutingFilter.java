package br.fitforge.gateway.filter;

import br.fitforge.gateway.security.AccountScope;
import java.util.Optional;
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
    private final AccountScope scope;

    public InternalRoutingFilter(@Value("${app.internal-key}") String key, AccountScope scope) {
        this.key = key;
        this.scope = scope;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal().map(scope::owner).defaultIfEmpty(Optional.empty()).flatMap(owner -> {
            var request = exchange.getRequest().mutate().headers(headers -> {
                headers.remove(HttpHeaders.AUTHORIZATION);
                headers.remove(INTERNAL_HEADER);
                headers.remove(AccountScope.HEADER);
                if (!key.isBlank()) headers.set(INTERNAL_HEADER, key);
                owner.ifPresent(value -> headers.set(AccountScope.HEADER, value));
            }).build();
            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
