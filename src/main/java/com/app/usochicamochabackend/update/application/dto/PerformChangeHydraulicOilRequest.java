package com.app.usochicamochabackend.update.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PerformChangeHydraulicOilRequest(
        Long machineId,
        LocalDateTime dateTime,
        String brand,
        Integer quantity,
        Integer currentHourMeter,
        Integer averageHoursChange
) {}