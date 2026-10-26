package com.app.usochicamochabackend.tire.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EvaluadorPresionLlanta Tests")
class EvaluadorPresionLlantaTest {

    private static final BigDecimal MIN = new BigDecimal("28");
    private static final BigDecimal MAX = new BigDecimal("33");

    @Test
    @DisplayName("Por debajo del mínimo es BAJA; los extremos del rango son OK")
    void evaluar_Bordes() {
        assertEquals(ResultadoPresionLlanta.BAJA, EvaluadorPresionLlanta.evaluar(new BigDecimal("27.9"), MIN, MAX));
        assertEquals(ResultadoPresionLlanta.OK, EvaluadorPresionLlanta.evaluar(new BigDecimal("28"), MIN, MAX));
        assertEquals(ResultadoPresionLlanta.OK, EvaluadorPresionLlanta.evaluar(new BigDecimal("33.0"), MIN, MAX));
        assertEquals(ResultadoPresionLlanta.ALTA, EvaluadorPresionLlanta.evaluar(new BigDecimal("33.1"), MIN, MAX));
    }

    @Test
    @DisplayName("Sin rango configurado no se evalúa")
    void evaluar_SinRango() {
        assertEquals(ResultadoPresionLlanta.SIN_RANGO, EvaluadorPresionLlanta.evaluar(new BigDecimal("30"), null, MAX));
        assertEquals(ResultadoPresionLlanta.SIN_RANGO, EvaluadorPresionLlanta.evaluar(new BigDecimal("30"), MIN, null));
    }

    @Test
    @DisplayName("El resumen prioriza BAJA, luego ALTA, luego OK")
    void resumir_Prioridad() {
        assertNull(EvaluadorPresionLlanta.resumir(List.of()));
        assertEquals(ResultadoPresionLlanta.BAJA, EvaluadorPresionLlanta.resumir(List.of(
                ResultadoPresionLlanta.OK, ResultadoPresionLlanta.ALTA, ResultadoPresionLlanta.BAJA)));
        assertEquals(ResultadoPresionLlanta.ALTA, EvaluadorPresionLlanta.resumir(List.of(
                ResultadoPresionLlanta.OK, ResultadoPresionLlanta.ALTA, ResultadoPresionLlanta.SIN_RANGO)));
        assertEquals(ResultadoPresionLlanta.OK, EvaluadorPresionLlanta.resumir(List.of(
                ResultadoPresionLlanta.OK, ResultadoPresionLlanta.SIN_RANGO)));
        assertEquals(ResultadoPresionLlanta.SIN_RANGO, EvaluadorPresionLlanta.resumir(List.of(
                ResultadoPresionLlanta.SIN_RANGO)));
    }
}
