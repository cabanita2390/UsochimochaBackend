package com.app.usochicamochabackend.update.application.dto;

import java.time.LocalDate;

public record ConsolidateHydraulicOilDTO(
        CurrentData currentData,
        Long id,
        String type,
        String brand,
        Integer quantity,
        Integer averageChangeHours,
        LocalDate dateLastUpdate,
        Double hourMeterLastUpdate,
        Double hourMeterNextUpdate,
        Double timeLastUpdateMouths,
        Double remainingHoursNextUpdateMouths,
        String status
) {}