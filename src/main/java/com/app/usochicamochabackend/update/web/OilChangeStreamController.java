package com.app.usochicamochabackend.update.web;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/oil_change/notifications")
public class OilChangeStreamController {

    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream() {
        return sink.asFlux()
                .map(data -> ServerSentEvent.<String>builder()
                        .event("oil-change-status")
                        .data(data)
                        .build()
                )
                .mergeWith(Flux.interval(Duration.ofSeconds(15))
                        .map(seq -> ServerSentEvent.<String>builder()
                                .event("keepalive")
                                .data("ping")
                                .build())
                );
    }

    public void sendNotification(String status) {
        sink.tryEmitNext(status);
    }
}