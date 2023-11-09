package com.sergdalm.springcloudgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergdalm.springcloudgateway.model.UserInfo;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class UserHeaderFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .filter(c -> c.getAuthentication() != null)
                .flatMap(c -> {
                    var jwt = (JwtAuthenticationToken) c.getAuthentication();
                    var request = exchange.getRequest();

                    var username = (String) jwt.getTokenAttributes().get("username");
                    var id = (String) jwt.getTokenAttributes().get("id");
                    if (StringUtils.hasText(username) && StringUtils.hasText(id)) {
                        var objectMapper = new ObjectMapper();
                        String userInfo;
                        try {
                            userInfo = objectMapper.writeValueAsString(new UserInfo(UUID.fromString(id), username));
                        } catch (JsonProcessingException e) {
                            return Mono.error(new RuntimeException(e));
                        }
                        request.mutate().header("X-User-Info", userInfo).build();
                    }

                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
