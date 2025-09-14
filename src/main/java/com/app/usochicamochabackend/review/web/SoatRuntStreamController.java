package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.dto.ExpirationNotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
@RequiredArgsConstructor
@RequestMapping("/soat/runt/notifications")
public class SoatRuntStreamController {

    private final Sinks.Many<ExpirationNotificationDTO> notificationsSink =
            Sinks.many().multicast().onBackpressureBuffer();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExpirationNotificationDTO> streamNotifications() {
        return notificationsSink.asFlux();
    }

    public void publish(ExpirationNotificationDTO notification) {
        notificationsSink.tryEmitNext(notification);
    }
}