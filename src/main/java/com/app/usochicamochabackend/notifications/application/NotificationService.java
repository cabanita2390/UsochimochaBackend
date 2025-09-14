package com.app.usochicamochabackend.notifications.application;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class NotificationService {

    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void notify(String event) {
        sink.tryEmitNext(event);
    }

    public Flux<String> getNotifications() {
        return sink.asFlux();
    }
}