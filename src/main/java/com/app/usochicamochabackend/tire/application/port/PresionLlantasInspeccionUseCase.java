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

    /**
     * Si las lecturas que llegan son las mismas ya guardadas para esa inspección de máquina (mismas
     * llantas y PSI, con las mismas reglas de limpieza que al registrar). Sirve para no confundir una
     * segunda inspección del día con un reenvío de la primera.
     */
    boolean mismasLecturasMaquina(Long idInspeccion, List<LecturaPresionLlantaRequest> lecturas);

    /** OK | BAJA | ALTA | SIN_RANGO, o null si la inspección no tiene lecturas. */
    String resumen(List<LecturaPresionLlantaResponse> lecturas);
}
