package com.app.usochicamochabackend.notifications.web;

import com.app.usochicamochabackend.notifications.application.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/new-data/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamNotifications() {
        return notificationService.getNotifications()
                .map(event -> event.isBlank() ? "new-data" : event);
    }

    @PostMapping("/notify")
    public void notifyClients(@RequestParam(defaultValue = "") String event) {
        notificationService.notify(event);
    }
}