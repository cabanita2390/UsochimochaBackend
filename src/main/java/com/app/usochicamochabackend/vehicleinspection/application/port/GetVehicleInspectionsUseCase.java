package com.app.usochicamochabackend.vehicleinspection.application.port;

import com.app.usochicamochabackend.vehicleinspection.application.dto.VehicleInspectionReportDTO;
import java.util.List;

public interface GetVehicleInspectionsUseCase {
    List<VehicleInspectionReportDTO> getInspectionsByType(Integer typeId);
    List<VehicleInspectionReportDTO> getMotoInspections();
}
