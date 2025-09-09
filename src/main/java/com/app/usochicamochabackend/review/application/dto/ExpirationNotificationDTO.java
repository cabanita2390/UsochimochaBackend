package com.app.usochicamochabackend.review.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;

public record ExpirationNotificationDTO(
        String type,   // "SOAT" o "RUNT"
        String message,
        MachineResponse machine
) {}