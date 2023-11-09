package com.sergdalm.springcloudgateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Import(SpringCloudGatewayApplication.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SpringCloudGatewayTest {

    private static ClientAndServer mockServer;

    private static int managementPort;

    @LocalServerPort
    private int port = 0;

    private WebTestClient webClient;

    private String baseUri;

    @BeforeAll
    public static void beforeClass() {
        managementPort = TestSocketUtils.findAvailableTcpPort();

        System.setProperty("test.port", String.valueOf(managementPort));

        mockServer = startClientAndServer(9000);
    }

    @AfterAll
    public static void afterClass() {
        System.clearProperty("test.port");

        mockServer.stop();
    }

    @BeforeEach
    public void setup() {
        baseUri = "http://localhost:" + port;
        this.webClient = WebTestClient.bindToServer()
                .responseTimeout(Duration.ofSeconds(10))
                .baseUrl(baseUri).build();
    }

    // Run sse-server/SseApplication before running this test.
    // This test shows that client receives server-send events as they were sent,
    // not when stream was closed.
    @Test
    public void whenSSEEndpointIsCalledThenEventStreamingBegins() {
        var result = webClient.get()
                .uri("/sse?key=2")
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(String.class);

        var responseNumbers = new ArrayList<Integer>();
        StepVerifier.create(result.getResponseBody())
                .assertNext(it -> {
                    var currentNumberResponse = Integer.parseInt(it.replace("2 Fixed delay task - ", ""));
                    responseNumbers.add(currentNumberResponse);
                })
                .assertNext(it -> {
                    var currentNumberResponse = Integer.parseInt(it.replace("2 Fixed delay task - ", ""));
                    Assertions.assertTrue(responseNumbers.get(0) < currentNumberResponse);
                    responseNumbers.add(currentNumberResponse);
                })
                .assertNext(it -> {
                    var currentNumberResponse = Integer.parseInt(it.replace("2 Fixed delay task - ", ""));
                    Assertions.assertTrue(responseNumbers.get(1) < currentNumberResponse);
                })
                .thenCancel()
                .verify();
    }

}
