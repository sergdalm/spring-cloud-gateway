package com.sergdalm.springcloudgateway;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

public class MockOAuth2ServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String MOCK_OAUTH_2_SERVER_BASE_URL = "mock-oauth2-server.baseUrl";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        var server = registerMockOAuth2Server(applicationContext);
        var baseUrl = server.baseUrl().toString().replaceAll("/$", "");

        TestPropertyValues
                .of(Map.of(MOCK_OAUTH_2_SERVER_BASE_URL, baseUrl))
                .applyTo(applicationContext);
    }

    private MockOAuth2Server registerMockOAuth2Server(ConfigurableApplicationContext applicationContext) {
        var server = new MockOAuth2Server();
        server.start();
        ((GenericApplicationContext) applicationContext).registerBean(MockOAuth2Server.class, () -> server);
        return server;
    }
}
