package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PerformChangeMotorOilRequest(
        Long machineId,
        LocalDateTime dateTime,
        BrandEntity brand,
        Integer quantity,
        Double currentHourMeter,
        Integer averageHoursChange
) {}