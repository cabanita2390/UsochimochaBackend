package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import jakarta.persistence.criteria.CriteriaBuilder;

public record PerformChangeMotorOilResponse(
        Long id,
        MachineResponse machine,
        String brand,
        Integer currentHourMeter,
        Integer quantity,
        Integer averageHoursChange
) {}