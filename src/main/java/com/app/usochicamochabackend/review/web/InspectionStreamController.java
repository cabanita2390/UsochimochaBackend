package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inspections")
public class InspectionStreamController {

        private final Sinks.Many<InspectionFormResponse> inspectionsSink = Sinks.many().multicast().onBackpressureBuffer();

        @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<InspectionFormResponse> streamInspections() {
            return inspectionsSink.asFlux();
        }

        public void publish(InspectionFormResponse inspection) {
            inspectionsSink.tryEmitNext(inspection);
        }

}
