package com.app.usochicamochabackend.review.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.order.application.dto.OrderWithoutInspectionResponse;
import com.app.usochicamochabackend.user.application.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record InspectionDTO(
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
        List<ImageDTO> images,
        List<OrderWithoutInspectionResponse> orders
) {}
