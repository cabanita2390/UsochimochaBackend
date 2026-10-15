package com.app.usochicamochabackend.substation.application.port;

/**
 * Avisos en tiempo real del módulo de subestaciones (la web refresca Dashboard, Detalle por
 * estación y Ejecuciones sin esperar a que el usuario recargue).
 */
public interface SubstationEventosPort {

    /** Una ejecución se registró o se editó; se envía cuando la transacción ya confirmó. */
    void ejecucionCambio(Long estacionId, Long ejecucionId, String tipo);
}
