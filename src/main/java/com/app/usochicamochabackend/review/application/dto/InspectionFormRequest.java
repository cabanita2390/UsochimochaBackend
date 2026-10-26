package com.app.usochicamochabackend.review.application.dto;

import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaRequest;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

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
        Long machineId,
        /** Presión medida por llanta (PSI), opcional según el perfil de llantas de la máquina. */
        List<LecturaPresionLlantaRequest> presionesLlantas
) {}