package com.app.usochicamochabackend.update.application.dto;

import java.time.LocalDate;

public record ConsolidateMotorOilDTO(
        CurrentData currentData,
        Long id,
        String type,
        String brand,
        Integer quantity,
        Integer averageChangeHours,
        LocalDate dateLastUpdate,
        Integer hourMeterLastUpdate,
        Integer hourMeterNextUpdate,
        Integer timeLastUpdateMouths,
        Integer remainingHoursNextUpdateMouths
) {}