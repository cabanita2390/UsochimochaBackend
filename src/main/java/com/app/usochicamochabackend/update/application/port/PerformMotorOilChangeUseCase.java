package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.update.application.dto.PerformChangeMotorOilRequest;
import com.app.usochicamochabackend.update.application.dto.PerformChangeMotorOilResponse;

public interface PerformMotorOilChangeUseCase {
    PerformChangeMotorOilResponse performMotorOilChange(PerformChangeMotorOilRequest request);
}