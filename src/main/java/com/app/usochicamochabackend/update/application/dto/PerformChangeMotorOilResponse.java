package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.time.LocalDateTime;

public record PerformChangeMotorOilResponse(
        Long id,
        MachineResponse machine,
        LocalDateTime timestamp,
        String brand,
        Integer quantity,
        Double currentHourMeter,
        Integer averageHoursChange
) {}