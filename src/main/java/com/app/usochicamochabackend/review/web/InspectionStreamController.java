package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Sinks;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/inspections")
public class InspectionStreamController {

        private final Sinks.Many<InspectionFormResponse> inspectionsSink = Sinks.many().multicast().onBackpressureBuffer();

        @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public SseEmitter streamInspections() {
            SseEmitter emitter = new SseEmitter(0L);
            log.info("Inspection stream subscribed");

            try {
                emitter.send(SseEmitter.event().data("stream_open"));
            } catch (Exception e) {
                log.error("Error sending stream_open", e);
                emitter.completeWithError(e);
                return emitter;
            }

            inspectionsSink.asFlux().subscribe(
                data -> {
                    try {
                        emitter.send(SseEmitter.event().data(data));
                    } catch (Exception e) {
                        log.warn("Error sending inspection data, likely client disconnected", e);
                        // Don't complete again if already completed
                    }
                },
                error -> {
                    log.error("Inspection stream error", error);
                    emitter.completeWithError(error);
                },
                () -> {
                    log.info("Inspection stream completed");
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

            emitter.onCompletion(() -> {
                log.info("Inspection stream completed");
                executor.shutdown();
            });
            emitter.onTimeout(() -> {
                log.info("Inspection stream timed out");
                executor.shutdown();
            });
            emitter.onError(throwable -> {
                log.error("Inspection stream error", throwable);
                executor.shutdown();
            });

            return emitter;
        }

        public void publish(InspectionFormResponse inspection) {
            log.debug("Publishing inspection: {}", inspection);
            inspectionsSink.tryEmitNext(inspection);
        }

}
