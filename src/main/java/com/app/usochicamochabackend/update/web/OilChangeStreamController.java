package com.app.usochicamochabackend.update.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/oil_change/notifications")
public class OilChangeStreamController {

    private final BlockingQueue<String> notificationsQueue = new LinkedBlockingQueue<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        log.info("Oil change stream subscribed");

        try {
            emitter.send(SseEmitter.event().data("stream_open"));
        } catch (Exception e) {
            log.error("Error sending stream_open", e);
            emitter.completeWithError(e);
            return emitter;
        }

        // Send existing notifications from queue
        String data;
        while ((data = notificationsQueue.poll()) != null) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                log.warn("Error sending oil change data, likely client disconnected", e);
                break;
            }
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment(""));
            } catch (Exception e) {
                log.warn("Error sending keep-alive, shutting down executor");
                executor.shutdown();
            }
        }, 0, 15, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            log.info("Oil change stream completed");
            executor.shutdown();
        });
        emitter.onTimeout(() -> {
            log.info("Oil change stream timed out");
            executor.shutdown();
        });
        emitter.onError(throwable -> {
            log.error("Oil change stream error", throwable);
            executor.shutdown();
        });

        return emitter;
    }

    public void sendNotification(String status) {
        log.debug("Sending oil change notification: {}", status);
        notificationsQueue.offer(status);
    }
}