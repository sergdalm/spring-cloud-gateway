package com.sergdalm.springcloudgateway.filter;

import com.sergdalm.springcloudgateway.jwt.JwtUtil;
import com.sergdalm.springcloudgateway.model.UserInfo;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthJwtFilter implements GatewayFilter {

    private final JwtUtil jwtUtil;

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public AuthJwtFilter(JwtUtil jwtUtil, WebClient.Builder webClientBuilder) {
        this.jwtUtil = jwtUtil;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getHeaders().containsKey("X-User-info")) {
            ServerHttpRequest request = exchange.getRequest();

            final String token = request.getHeaders().getOrEmpty("X-User-info").get(0);

            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getClaims(token);
                exchange.getRequest().mutate().header("id", String.valueOf(claims.get("id"))).build();
                exchange.getRequest().mutate().header("name", String.valueOf(claims.get("name"))).build();
                return chain.filter(exchange);
            }
        }

        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            return response.setComplete();
        }

        String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/auth/token?token=" + authHeader)
                .retrieve().bodyToMono(UserInfo.class)
                .map(userInfo -> {
                    var token = jwtUtil.generateToken(userInfo);
                    exchange.getResponse()
                            .getHeaders()
                            .put("X-User-info", List.of(token));
                    return exchange;
                }).flatMap(chain::filter);

    }

}
