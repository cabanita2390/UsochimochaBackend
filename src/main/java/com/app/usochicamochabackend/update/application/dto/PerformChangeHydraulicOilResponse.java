package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;

public record PerformChangeHydraulicOilResponse(
        Long id,
        MachineResponse machine,
        BrandEntity brand,
        Double currentHourMeter,
        Integer quantity,
        Integer averageHoursChange
) {}