package com.app.usochicamochabackend.review.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.user.application.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record InspectionResponse(Long id,
                                 String UUID,
                                 LocalDateTime dateStamp,
                                 String hourMeter,
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
                                 UserResponse user,
                                 MachineResponse machine,
                                 ImagesDTO images) {
}
