package com.app.usochicamochabackend.vehicleinspection.application.port;

import com.app.usochicamochabackend.vehicleinspection.application.dto.VehicleInspectionReportDTO;
import java.util.List;

public interface GetVehicleInspectionsUseCase {
    List<VehicleInspectionReportDTO> getInspectionsByType(Integer typeId);

    /** Una fila por placa: inspección con {@code fecha_registro} más reciente (evita duplicados por varios {@code id_vehiculo}). */
    List<VehicleInspectionReportDTO> getMotoInspectionsLatestPerVehicle();

    /** Todas las inspecciones de motos, más recientes primero (historial). */
    List<VehicleInspectionReportDTO> getMotoInspectionsHistory();
}
