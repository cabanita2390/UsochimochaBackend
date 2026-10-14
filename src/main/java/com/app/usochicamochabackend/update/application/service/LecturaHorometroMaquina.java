package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity;
import com.app.usochicamochabackend.update.infrastructure.repository.OilChangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * "Horómetro actual" de una máquina para el Consolidado de aceites y sus alertas: la
 * lectura más alta tomada en la máquina entre la última inspección y el último cambio de
 * aceite (motor e hidráulico), con la fecha de esa lectura.
 *
 * Antes solo se usaba la inspección: si el cambio de aceite se registraba después con un
 * horómetro mayor, el Consolidado mostraba un horómetro actual MENOR al del cambio y
 * "horas restantes" mayores al propio intervalo. No se usa machines.horometro_actual
 * porque solo avanza (un error de digitación en otro módulo quedaría para siempre).
 * Un cambio atrasado (horómetro menor) no afecta la lectura.
 */
@Component
@RequiredArgsConstructor
public class LecturaHorometroMaquina {

    public record Lectura(Double horometro, LocalDateTime fecha) {}

    private final InspectionRepository inspectionRepository;
    private final OilChangeRepository oilChangeRepository;

    /** Null si la máquina no tiene ni inspecciones ni cambios de aceite con horómetro. */
    public Lectura ultima(Long machineId) {
        InspectionEntity inspeccion = inspectionRepository.getLastInspection(machineId);
        OilChangeEntity motor = oilChangeRepository.getLastMotorOilChangeByMachineId(machineId);
        OilChangeEntity hidraulico = oilChangeRepository.getLastHydraulicOilChangeByMachineId(machineId);

        Lectura desdeInspeccion = inspeccion != null && inspeccion.getHourMeter() != null
                ? new Lectura(inspeccion.getHourMeter(), inspeccion.getDateStamp())
                : null;

        return Stream.of(desdeInspeccion, desdeCambio(motor), desdeCambio(hidraulico))
                .filter(l -> l != null)
                // A igual horómetro gana la inspección (primera del Stream): es la lectura "oficial".
                .reduce((a, b) -> b.horometro() > a.horometro() ? b : a)
                .orElse(null);
    }

    private static Lectura desdeCambio(OilChangeEntity cambio) {
        return cambio != null && cambio.getHourMeter() != null
                ? new Lectura(cambio.getHourMeter(), cambio.getDateStamp())
                : null;
    }
}
