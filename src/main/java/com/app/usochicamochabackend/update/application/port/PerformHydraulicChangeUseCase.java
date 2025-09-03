package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.PerformChangeHydraulicOilResponse;
import com.app.usochicamochabackend.update.application.dto.PerformChangeMotorOilRequest;
import com.app.usochicamochabackend.update.application.dto.PerformChangeMotorOilResponse;

public interface PerformHydraulicChangeUseCase {
    PerformChangeHydraulicOilResponse performChangeHydraulicOil(PerformChangeMotorOilRequest request);
}