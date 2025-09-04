package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;

public record PerformChangeHydraulicOilResponse(
        Long id,
        MachineResponse machine,
        String brand,
        Integer currentHourMeter,
        Integer quantity,
        Integer averageHoursChange
) {}