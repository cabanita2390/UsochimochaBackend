package com.app.usochicamochabackend.vehicleinspection.application.port;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionRequest;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionResponse;

public interface CreateVehiculoInspectionUseCase {

    VehiculoInspectionResponse create(VehiculoInspectionRequest request, UserPrincipal inspector);
}
