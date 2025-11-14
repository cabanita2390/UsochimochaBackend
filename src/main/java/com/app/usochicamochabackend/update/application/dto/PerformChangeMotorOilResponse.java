package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.time.LocalDateTime;

public record PerformChangeMotorOilResponse(
        Long id,
        MachineResponse machine,
        LocalDateTime timestamp,
        BrandEntity brand,
        Double quantity,
        Double currentHourMeter,
        Integer averageHoursChange
) {}