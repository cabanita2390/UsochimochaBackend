package com.app.usochicamochabackend.update.application.dto;

import java.time.LocalDate;

public record PerformChangeMotorOilRequest(
        Long machineId,
        LocalDate dateTime,
        String brand,
        Integer quantity,
        Integer currentHourMeter,
        Integer averageHoursChange
) {}