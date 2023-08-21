package com.sergdalm.springcloudgateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Import(SpringCloudGatewayApplication.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SpringCloudGatewayTest {

    private static int managementPort;

    @LocalServerPort
    private int port = 0;

    private WebTestClient webClient;

    private String baseUri;

    @BeforeAll
    public static void beforeClass() {
        managementPort = TestSocketUtils.findAvailableTcpPort();

        System.setProperty("test.port", String.valueOf(managementPort));
    }

    @AfterAll
    public static void afterClass() {
        System.clearProperty("test.port");
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


    // Run admin/AdminApplication and first-microservice/FirstMicroserviceApplication before running this test.
    @Test
    public void makeRequestWithAuthorizationHeader() {
        var result = webClient.get()
                .uri("/first")
                .header("authorization", "111")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valuesMatch("X-User-info", ".+")
                .returnResult(String.class);
        var XUserInfoHeader = result.getResponseHeaders().get("X-User-info");

        webClient.get()
                .uri("/first")
                .header("X-User-info", XUserInfoHeader.get(0))
                .exchange()
                .expectStatus().isOk();
    }

    // Run second-microservice/SecondMicroserviceApplication before running this test.
    @Test
    public void makeRequestWithoutAuthorizationHeader() {
        webClient.get()
                .uri("/second")
                .exchange()
                .expectStatus().isOk();
    }

}
