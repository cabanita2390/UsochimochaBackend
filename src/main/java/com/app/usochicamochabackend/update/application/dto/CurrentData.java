package com.app.usochicamochabackend.update.application.dto;

import java.time.LocalDateTime;

public record CurrentData(
        String belongsTo,
        String machineName,
        Double currentHourMeter,
        LocalDateTime lastUpdate
) {}