package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PerformChangeMotorOilRequest(
        Long machineId,
        LocalDateTime dateTime,
        Long brandId,
        Integer quantity,
        Double currentHourMeter,
        Integer averageHoursChange
) {}