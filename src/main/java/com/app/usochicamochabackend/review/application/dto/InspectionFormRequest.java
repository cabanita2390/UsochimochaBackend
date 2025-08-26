package com.app.usochicamochabackend.review.application.dto;

import java.time.LocalDateTime;

public record InspectionFormRequest(
        String UUID,
        LocalDateTime dateStamp,
        String hourmeter,
        String leakStatus,
        String brakeStatus,
        String beltsPulleysStatus,
        String tireLanesStatus,
        String carIgnitionStatus,
        String electricalStatus,
        String mechanicalStatus,
        String temperatureStatus,
        String oilStatus,
        String hydraulicStatus,
        String coolantStatus,
        String structuralStatus,
        String expirationDateFireExtinguisher,
        String observations,
        Long userId,
        Long machineId
) {}