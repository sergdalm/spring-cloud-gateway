package com.sergdalm.springcloudgateway;

import no.nav.security.mock.oauth2.MockOAuth2Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

import static com.sergdalm.springcloudgateway.MockOAuth2ServerInitializer.MOCK_OAUTH_2_SERVER_BASE_URL;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SpringCloudGatewayApplication.class,
        properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=${" + MOCK_OAUTH_2_SERVER_BASE_URL + "}/issuer1"
)
@ContextConfiguration(initializers = {MockOAuth2ServerInitializer.class})
public class AuthTokenTest {

    private final static UUID USER_ID = UUID.fromString("ce7efa7e-ec53-4cd9-b649-5ab2ff7679fb");
    private final static String USER_NAME = "name";

    private static ClientAndServer firstMockServer;
    private static ClientAndServer secondMockServer;

    @Autowired
    private WebTestClient webClient;

    @Autowired(required = false)
    private MockOAuth2Server mockOAuth2Server;

    @BeforeAll
    public static void beforeClass() {
        firstMockServer = startClientAndServer(8081);
        secondMockServer = startClientAndServer(8082);
    }

    @AfterAll
    public static void afterClass() {
        firstMockServer.stop();
        secondMockServer.stop();
    }

    @Test
    @DisplayName("first microservice api should return 200 when valid token is present")
    public void validTokenShouldReturn200Ok() {
        try (var mockServer = new MockServerClient("127.0.0.1", 8081)) {
            mockServer.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/first/hello")
                                    .withHeader("X-User-Info",
                                            "{\"id\":\"ce7efa7e-ec53-4cd9-b649-5ab2ff7679fb\",\"name\":\"name\"}"),
                            exactly(1))
                    .respond(new HttpResponse().withBody("Hello!").withStatusCode(200));

            var token = mockOAuth2Server.issueToken("issuer1", "foo", "quillis",
                    Map.of("id", USER_ID,
                            "username", USER_NAME));
            webClient.get()
                    .uri("/first/hello")
                    .headers(headers -> headers.setBearerAuth(token.serialize()))
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class).isEqualTo("Hello!");
        }
    }

    @Test
    @DisplayName("first microservice api should return 401 when invalid token is present")
    public void invalidTokenShouldReturn401() {
        webClient.get()
                .uri("/first/hello")
                .headers(headers -> headers.setBearerAuth("invalid_token"))
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    // This test  fails
    @Test
    @DisplayName("second microservice api should return 200 without token")
    public void shouldReturn200OkWithoutToken() {
        try (var mockServer = new MockServerClient("127.0.0.1", 8082)) {
            mockServer.when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/second/hello"),
                            exactly(1))
                    .respond(new HttpResponse().withBody("Hello!").withStatusCode(200));

            webClient.get()
                    .uri("/second/hello")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class).isEqualTo("Hello!");
        }
    }
}
