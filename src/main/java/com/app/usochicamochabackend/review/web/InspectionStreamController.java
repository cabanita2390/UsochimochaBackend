package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.dto.InspectionResponse;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inspections")
public class InspectionStreamController {

        private final Sinks.Many<InspectionResponse> inspectionsSink = Sinks.many().multicast().onBackpressureBuffer();

        @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<InspectionResponse> streamInspections() {
            return inspectionsSink.asFlux();
        }

        public void publish(InspectionResponse inspection) {
            inspectionsSink.tryEmitNext(inspection);
        }

}
