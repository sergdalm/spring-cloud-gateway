package com.sergdalm.springcloudgateway.config;

import com.sergdalm.springcloudgateway.filter.AuthJwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

    private final AuthJwtFilter authJwtFilter;

    @Autowired
    public GatewayConfiguration(AuthJwtFilter authJwtFilter) {
        this.authJwtFilter = authJwtFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // first microservice is available only with token
                .route("first-microservice", r -> r.path("/first/**")
                        .filters(f -> f.filter(authJwtFilter))
                        .uri("http://localhost:8081/"))

                // second microservice is available without token
                .route("second-microservice", r -> r.path("/second/**")
                        .uri("http://localhost:8082/"))

                // sse service is available without token
                .route("sse", r -> r.path("/sse/**")
                        .uri("http://localhost:9999/"))

                .build();
    }

}
