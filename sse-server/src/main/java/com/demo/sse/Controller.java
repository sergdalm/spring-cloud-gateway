package com.demo.sse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class Controller {

    private static int count = 0;

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sse(@RequestParam String key) {
        return Flux.interval(Duration.ofSeconds(2))
                .map((i) -> key + " Fixed delay task - " + count++);

    }
}
