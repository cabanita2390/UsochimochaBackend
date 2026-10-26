package com.app.usochicamochabackend.tire.application.port;

import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaRequest;
import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Lecturas de presión ligadas a las inspecciones. Lo usan los servicios de inspección de
 * vehículo/moto ({@code inspeccion_pre_operativa}) y de maquinaria ({@code inspections}).
 */
public interface PresionLlantasInspeccionUseCase {

    /** Evalúa y guarda las lecturas. Nunca falla por una lectura inválida: la omite. */
    List<LecturaPresionLlantaResponse> registrarParaInspeccionVehiculo(
            Integer vehicleId, Long idInspeccion, List<LecturaPresionLlantaRequest> lecturas);

    List<LecturaPresionLlantaResponse> registrarParaInspeccionMaquina(
            Long machineId, Long idInspeccion, List<LecturaPresionLlantaRequest> lecturas);

    Map<Long, List<LecturaPresionLlantaResponse>> lecturasPorInspeccionVehiculo(Collection<Long> idsInspeccion);

    Map<Long, List<LecturaPresionLlantaResponse>> lecturasPorInspeccionMaquina(Collection<Long> idsInspeccion);

    /** OK | BAJA | ALTA | SIN_RANGO, o null si la inspección no tiene lecturas. */
    String resumen(List<LecturaPresionLlantaResponse> lecturas);
}
