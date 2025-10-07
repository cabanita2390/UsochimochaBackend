package com.app.usochicamochabackend.notifications.web;

import com.app.usochicamochabackend.notifications.application.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/new-data/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        SseEmitter emitter = new SseEmitter(0L);
        log.info("New data notifications stream subscribed");

        try {
            emitter.send(SseEmitter.event().data("stream_open"));
        } catch (Exception e) {
            log.error("Error sending stream_open", e);
            emitter.completeWithError(e);
            return emitter;
        }

        Disposable disposable = notificationService.getNotifications()
                .map(event -> event.isBlank() ? "new-data" : event)
                .subscribe(
                    data -> {
                        try {
                            emitter.send(SseEmitter.event().data(data));
                        } catch (Exception e) {
                            log.warn("Error sending notification data, likely client disconnected", e);
                            // Don't complete again if already completed
                        }
                    },
                    error -> {
                        log.error("New data notifications stream error", error);
                        emitter.completeWithError(error);
                    },
                    () -> {
                        log.info("New data notifications stream completed");
                        emitter.complete();
                    }
                );

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment(""));
            } catch (Exception e) {
                log.warn("Error sending keep-alive, shutting down executor");
                executor.shutdown();
            }
        }, 0, 15, TimeUnit.SECONDS);

        Runnable cleanup = () -> {
            log.info("Cleaning up new data notifications stream");
            disposable.dispose();
            executor.shutdown();
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(throwable -> {
            log.error("New data notifications stream error", throwable);
            cleanup.run();
        });

        return emitter;
    }

    @PostMapping("/notify")
    public void notifyClients(@RequestParam(defaultValue = "") String event) {
        log.debug("Notifying clients: {}", event);
        notificationService.notify(event);
    }
}