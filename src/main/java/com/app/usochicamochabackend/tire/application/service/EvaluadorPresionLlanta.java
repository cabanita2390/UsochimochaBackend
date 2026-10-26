package com.app.usochicamochabackend.tire.application.service;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Reglas puras de evaluación de presión. El móvil aplica las mismas para la alerta sin conexión;
 * el servidor recalcula al registrar y es la fuente de verdad.
 */
public final class EvaluadorPresionLlanta {

    private EvaluadorPresionLlanta() {
    }

    public static ResultadoPresionLlanta evaluar(BigDecimal psi, BigDecimal psiMin, BigDecimal psiMax) {
        if (psiMin == null || psiMax == null) {
            return ResultadoPresionLlanta.SIN_RANGO;
        }
        if (psi.compareTo(psiMin) < 0) {
            return ResultadoPresionLlanta.BAJA;
        }
        if (psi.compareTo(psiMax) > 0) {
            return ResultadoPresionLlanta.ALTA;
        }
        return ResultadoPresionLlanta.OK;
    }

    /**
     * Estado de la inspección completa: BAJA si alguna llanta está baja (es lo que hay que calibrar),
     * luego ALTA, luego OK; SIN_RANGO si ninguna lectura se pudo evaluar; null si no hubo lecturas.
     */
    public static ResultadoPresionLlanta resumir(Collection<ResultadoPresionLlanta> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            return null;
        }
        if (resultados.contains(ResultadoPresionLlanta.BAJA)) {
            return ResultadoPresionLlanta.BAJA;
        }
        if (resultados.contains(ResultadoPresionLlanta.ALTA)) {
            return ResultadoPresionLlanta.ALTA;
        }
        if (resultados.contains(ResultadoPresionLlanta.OK)) {
            return ResultadoPresionLlanta.OK;
        }
        return ResultadoPresionLlanta.SIN_RANGO;
    }
}
