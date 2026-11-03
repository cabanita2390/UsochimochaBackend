package com.app.usochicamochabackend.review.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaResponse;
import com.app.usochicamochabackend.user.application.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record InspectionFormResponse(
        Long id,
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
        UserResponse user,
        MachineResponse machine,
        /** OK | BAJA | ALTA | SIN_RANGO; null si la inspección no registró presiones. */
        String presionLlantasEstado,
        List<LecturaPresionLlantaResponse> presionesLlantas
) {}
