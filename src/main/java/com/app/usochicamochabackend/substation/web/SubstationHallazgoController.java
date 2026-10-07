package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.ResolverHallazgoRequest;
import com.app.usochicamochabackend.substation.application.dto.SeguimientoResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationHallazgoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/substation/hallazgos")
@RequiredArgsConstructor
@Tag(name = "Subestaciones - Hallazgos", description = "Seguimiento de hallazgos: ABIERTO → EN_PROCESO → RESUELTO.")
public class SubstationHallazgoController {

    private final SubstationHallazgoUseCase hallazgoUseCase;

    @PutMapping("/{ejecucionId}/en-proceso")
    @Operation(summary = "Marcar hallazgo en proceso",
            description = "404 si la ejecución no existe; 409 si es CONFORME o el hallazgo ya está RESUELTO.")
    public ResponseEntity<SeguimientoResponse> marcarEnProceso(
            @PathVariable Long ejecucionId, Authentication authentication) {
        return ResponseEntity.ok(hallazgoUseCase.marcarEnProceso(ejecucionId, usuario(authentication)));
    }

    @PutMapping("/{ejecucionId}/resolver")
    @Operation(summary = "Resolver hallazgo",
            description = "observacionesCierre obligatorio. resueltoEnEjecucionId (opcional) debe ser de la misma "
                    + "estación y posterior; no se combina con resueltoMismaVisita=true. "
                    + "404 si la ejecución no existe; 409 si es CONFORME o ya está RESUELTO.")
    public ResponseEntity<SeguimientoResponse> resolver(
            @PathVariable Long ejecucionId, @RequestBody @Valid ResolverHallazgoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(hallazgoUseCase.resolver(ejecucionId, request, usuario(authentication)));
    }

    private UserPrincipal usuario(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }
}
