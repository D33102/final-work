package com.proj.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder,
            @Value("${services.auth:http://localhost:8083}") String auth,
            @Value("${services.user:http://localhost:8081}") String user,
            @Value("${services.wallet:http://localhost:8080}") String wallet,
            @Value("${services.payment:http://localhost:8082}") String payment,
            @Value("${services.ledger:http://localhost:8084}") String ledger) {

        return builder.routes()
                .route("auth", r -> r.path("/api/auth/**").uri(auth))
                .route("users", r -> r.path("/api/users/**").uri(user))
                .route("wallets", r -> r.path("/api/wallets/**").uri(wallet))
                .route("payment", r -> r.path("/api/payment/**").uri(payment))
                .route("ledger", r -> r.path("/api/ledger/**").uri(ledger))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter(
            @Value("${gateway.allowed-origin:http://localhost:3000}") String allowedOrigin) {

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(allowedOrigin);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
