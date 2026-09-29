package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCronogramaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/substation/cronograma")
@RequiredArgsConstructor
@Tag(name = "Subestaciones - Cronograma", description = "Cronograma anual editable: borrador → publicación al móvil.")
public class SubstationCronogramaController {

    private final SubstationCronogramaUseCase cronogramaUseCase;

    @GetMapping
    @Operation(summary = "Cronograma anual (grilla estación × mes)",
            description = "Citas BORRADOR y PUBLICADA del año (nunca RETIRADA ni de estaciones inactivas), con su "
                    + "primera ejecución; estado de publicación (última publicación, borrador, puedeDeshacer) y "
                    + "anioActual/mesActual del servidor para saber qué meses están cerrados. disciplina es "
                    + "opcional y solo filtra las citas; el borrador se cuenta completo.")
    public ResponseEntity<CronogramaResponse> obtenerCronograma(
            @RequestParam Integer anio,
            @RequestParam(required = false) String disciplina) {
        return ResponseEntity.ok(cronogramaUseCase.obtenerCronograma(anio, disciplina));
    }
}
