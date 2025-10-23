package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/inspections")
public class InspectionStreamController {

        private final BlockingQueue<InspectionFormResponse> inspectionsQueue = new LinkedBlockingQueue<>();

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

            // Send existing data from queue
            InspectionFormResponse data;
            while ((data = inspectionsQueue.poll()) != null) {
                try {
                    emitter.send(SseEmitter.event().data(data));
                } catch (Exception e) {
                    log.warn("Error sending inspection data, likely client disconnected", e);
                    break;
                }
            }

            // For new data, since it's not reactive, we can't easily subscribe. For simplicity, this basic implementation sends existing data.
            // In a real scenario, you might need a more sophisticated mechanism or use a different approach.

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
            inspectionsQueue.offer(inspection);
        }

}
