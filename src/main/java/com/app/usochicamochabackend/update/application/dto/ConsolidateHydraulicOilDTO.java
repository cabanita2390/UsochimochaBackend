package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;

import java.time.LocalDate;

public record ConsolidateHydraulicOilDTO(
        CurrentData currentData,
        Long id,
        String type,
        BrandEntity brand,
        Double quantity,
        Integer averageChangeHours,
        LocalDate dateLastUpdate,
        Double hourMeterLastUpdate,
        Double hourMeterNextUpdate,
        Double timeLastUpdateMouths,
        Double remainingHoursNextUpdateMouths,
        String status
) {}