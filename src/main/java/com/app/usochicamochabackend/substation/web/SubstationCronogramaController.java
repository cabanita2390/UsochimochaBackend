package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.AsignacionResultado;
import com.app.usochicamochabackend.substation.application.dto.AsignarCitasRequest;
import com.app.usochicamochabackend.substation.application.dto.CopiarAnioRequest;
import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCronogramaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/citas")
    @Operation(summary = "Asignar citas (panel de celda o asignación masiva)",
            description = "Solo ADMIN. Crea las citas estación × mes en BORRADOR (el móvil no las ve hasta publicar). "
                    + "Omite sin fallar los duplicados, los meses cerrados y las estaciones inactivas, y los cuenta en "
                    + "la respuesta. 400 si la actividad no existe o está inactiva, si alguna estación no existe o si "
                    + "un mes está fuera de 1..12.")
    public ResponseEntity<AsignacionResultado> asignar(
            @RequestBody @Valid AsignarCitasRequest request, Authentication authentication) {
        return ResponseEntity.ok(cronogramaUseCase.asignar(request, usuario(authentication)));
    }

    @DeleteMapping("/citas/{id}")
    @Operation(summary = "Quitar una cita",
            description = "Solo ADMIN. BORRADOR: se descarta. PUBLICADA: queda \"se quitará al publicar\" y el móvil "
                    + "la sigue viendo hasta publicar. 404 si la cita no está en el cronograma; 409 si ya tiene "
                    + "ejecución o su mes está cerrado.")
    public ResponseEntity<Void> quitar(@PathVariable Long id, Authentication authentication) {
        cronogramaUseCase.quitar(id, usuario(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/citas/{id}/restaurar")
    @Operation(summary = "Restaurar una cita marcada para quitar",
            description = "Solo ADMIN. 404 si la cita no está en el cronograma; 409 si no estaba marcada para quitar.")
    public ResponseEntity<Void> restaurar(@PathVariable Long id, Authentication authentication) {
        cronogramaUseCase.restaurar(id, usuario(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/copiar")
    @Operation(summary = "Copiar un año como borrador",
            description = "Solo ADMIN. Copia las citas PUBLICADA del origen (solo estaciones y actividades activas) al "
                    + "destino, en BORRADOR. 400 si origen = destino; 409 si el destino ya tiene citas publicadas.")
    public ResponseEntity<AsignacionResultado> copiarAnio(
            @RequestBody @Valid CopiarAnioRequest request, Authentication authentication) {
        return ResponseEntity.ok(cronogramaUseCase.copiarAnio(request, usuario(authentication)));
    }

    @DeleteMapping("/borrador")
    @Operation(summary = "Descartar el borrador del año",
            description = "Solo ADMIN. Las altas en BORRADOR se dan de baja y las citas marcadas para quitar vuelven "
                    + "a la normalidad. Nunca toca citas con ejecución.")
    public ResponseEntity<Void> descartarBorrador(@RequestParam Integer anio, Authentication authentication) {
        cronogramaUseCase.descartarBorrador(anio, usuario(authentication));
        return ResponseEntity.noContent().build();
    }

    private UserPrincipal usuario(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }
}
