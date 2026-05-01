package com.app.usochicamochabackend.moto.application.service;

import com.app.usochicamochabackend.moto.application.dto.MotoMonitoringDTO;
import com.app.usochicamochabackend.moto.application.port.MotoMonitoringUseCase;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.DocumentacionYElementosEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspPreOperativaEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.DocumentacionYElementosRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspPreOperativaRepository;
import com.app.usochicamochabackend.update.infrastructure.entity.VehicleOilChangeEntity;
import com.app.usochicamochabackend.update.infrastructure.repository.VehicleOilChangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MotoMonitoringService implements MotoMonitoringUseCase {

    private final VehicleRepository vehicleRepository;
    private final InspPreOperativaRepository inspectionRepository;
    private final DocumentacionYElementosRepository documentRepository;
    private final VehicleOilChangeRepository oilChangeRepository;

    @Override
    public List<MotoMonitoringDTO> getConsolidatedMonitoring() {
        List<VehicleEntity> motos = vehicleRepository.findAllByTipoName("MOTOCICLETA");
        List<MotoMonitoringDTO> result = new ArrayList<>();

        for (VehicleEntity moto : motos) {
            result.add(buildMonitoringDTO(moto));
        }

        return result;
    }

    private MotoMonitoringDTO buildMonitoringDTO(VehicleEntity moto) {
        Optional<InspPreOperativaEntity> lastInsp = inspectionRepository.findLatestByVehicleId(moto.getIdVehiculo());
        
        Long daysSinceReport = null;
        LocalDateTime lastReportDate = null;
        String estadoMoto = "Óptimo";
        String novedad = "Ninguna";

        if (lastInsp.isPresent()) {
            InspPreOperativaEntity insp = lastInsp.get();
            lastReportDate = insp.getFechaRegistro();
            daysSinceReport = ChronoUnit.DAYS.between(lastReportDate.toLocalDate(), LocalDate.now());
            estadoMoto = insp.getEstadoVehiculo() != null ? insp.getEstadoVehiculo() : "Regular";
            novedad = (insp.getObservacionesFinales() != null && !insp.getObservacionesFinales().isBlank()) 
                      ? insp.getObservacionesFinales() : "Ninguna";
        }

        return new MotoMonitoringDTO(
            moto.getBelongsTo(),
            null, // Responsable (puedes ajustarlo si tienes un campo de asignación)
            moto.getPlaca(),
            moto.getKilometrajeActual(),
            getDocumentStatus(moto.getIdVehiculo(), "SOAT"),
            getDocumentStatus(moto.getIdVehiculo(), "TECNOMECANICA"),
            getOilStatus(moto),
            estadoMoto,
            lastReportDate,
            daysSinceReport,
            novedad
        );
    }

    private MotoMonitoringDTO.DocumentStatus getDocumentStatus(Integer vehicleId, String type) {
        Optional<DocumentacionYElementosEntity> doc = documentRepository.findLatestByVehiculoAndTipo(vehicleId, type);
        if (doc.isEmpty()) return null;

        LocalDate expiry = doc.get().getFechaVencimiento();
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
        String estado = (days >= 15) ? "Vigente" : (days >= 0 ? "Próximo a Vencer" : "Vencido");

        return new MotoMonitoringDTO.DocumentStatus(expiry, days, estado);
    }

    private MotoMonitoringDTO.OilStatus getOilStatus(VehicleEntity moto) {
        Optional<VehicleOilChangeEntity> lastChange = oilChangeRepository.findLatestByVehicleId(moto.getIdVehiculo());
        if (lastChange.isEmpty()) return null;

        VehicleOilChangeEntity change = lastChange.get();
        Integer kmAtChange = change.getKmAtChange();
        Integer nextChangeKm = kmAtChange + change.getIntervalKm();
        Integer kmActual = moto.getKilometrajeActual() != null ? moto.getKilometrajeActual() : 0;
        Integer kmRemaining = nextChangeKm - kmActual;

        String estado = (kmRemaining > 500) ? "OK" : (kmRemaining >= 0 ? "Próximo a Cambio" : "Cambio de Aceite");

        return new MotoMonitoringDTO.OilStatus(
            change.getDateStamp().toLocalDate(),
            kmAtChange,
            nextChangeKm,
            kmRemaining,
            change.getAirFilterChanged(),
            estado
        );
    }
}
