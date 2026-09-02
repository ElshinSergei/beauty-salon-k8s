package ru.elshin.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        // Ограничиваем по IP адресу
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());

        // Или, если хочешь ограничивать по пользователю из JWT:
        // return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-Id"));
    }
}
