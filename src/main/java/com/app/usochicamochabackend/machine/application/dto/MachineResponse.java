package com.app.usochicamochabackend.machine.application.dto;

import com.app.usochicamochabackend.review.application.dto.InspectionResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MachineResponse(Long id, String name, String belongsTo, String model, LocalDate soat, String brand, LocalDate runt, String numEngine, String numInterIdentification) {
}
