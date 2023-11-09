package com.sergdalm.springcloudgateway.config;

import com.sergdalm.springcloudgateway.filter.UserHeaderFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("first-microservice", r -> r.path("/first/**")
                        .uri("http://localhost:8081/"))

                .route("second-microservice", r -> r.path("/second/**")
                        .uri("http://localhost:8082/"))

                .route("sse", r -> r.path("/sse/**")
                        .uri("http://localhost:9999/"))
                .build();
    }

    @Bean
    public UserHeaderFilter userHeaderFilter() {
        return new UserHeaderFilter();
    }

}
