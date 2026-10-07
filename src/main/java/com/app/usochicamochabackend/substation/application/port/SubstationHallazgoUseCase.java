package com.app.usochicamochabackend.substation.application.port;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.ResolverHallazgoRequest;
import com.app.usochicamochabackend.substation.application.dto.SeguimientoResponse;

/**
 * Seguimiento de hallazgos desde la web.
 * Ciclo: ABIERTO → EN_PROCESO → RESUELTO, o ABIERTO → RESUELTO. Un RESUELTO no se reabre.
 */
public interface SubstationHallazgoUseCase {

    SeguimientoResponse marcarEnProceso(Long ejecucionId, UserPrincipal usuario);

    SeguimientoResponse resolver(Long ejecucionId, ResolverHallazgoRequest request, UserPrincipal usuario);
}
