package com.app.usochicamochabackend.update.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PerformChangeMotorOilRequest(
        Long machineId,
        LocalDateTime dateTime,
        String brand,
        Integer quantity,
        Double currentHourMeter,
        Integer averageHoursChange
) {}