package com.app.usochicamochabackend.review.application.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;

public record InspectionFormRequest(
        String UUID,
        Boolean isUnexpected,
        LocalDateTime dateStamp,
        Double hourMeter,
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
        String greasingAction,
        String greasingObservations,
        String observations,
        Long userId,
        Long machineId
) {}