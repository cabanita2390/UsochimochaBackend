package com.app.usochicamochabackend.substation.application.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CalendarioMantenimientoTest {

    private static CalendarioMantenimiento en(String instanteUtc) {
        return new CalendarioMantenimiento(Clock.fixed(Instant.parse(instanteUtc), CalendarioMantenimiento.ZONA));
    }

    @Test
    void mesActualSaleDeLaHoraDeColombia_noDeUtc() {
        // 1-oct 03:00 UTC = 30-sep 22:00 en Bogotá: todavía es septiembre.
        CalendarioMantenimiento cal = en("2026-10-01T03:00:00Z");
        assertEquals(2026, cal.anioActual());
        assertEquals(9, cal.mesActual());
    }

    @Test
    void mesCerrado_soloMesesAnterioresDelAnioActual() {
        CalendarioMantenimiento cal = en("2026-09-15T15:00:00Z");
        assertTrue(cal.mesCerrado(2026, 8));
        assertFalse(cal.mesCerrado(2026, 9)); // mes en curso
        assertFalse(cal.mesCerrado(2026, 10));
    }

    @Test
    void mesCerrado_anioPasadoCompletoYAnioFuturoAbierto() {
        CalendarioMantenimiento cal = en("2026-01-10T15:00:00Z");
        assertTrue(cal.mesCerrado(2025, 12));
        assertFalse(cal.mesCerrado(2026, 1));
        assertFalse(cal.mesCerrado(2027, 1));
    }

    @Test
    void enDiciembre_noviembreEstaCerradoYEneroSiguienteAbierto() {
        CalendarioMantenimiento cal = en("2026-12-31T15:00:00Z");
        assertTrue(cal.mesCerrado(2026, 11));
        assertFalse(cal.mesCerrado(2026, 12));
        assertFalse(cal.mesCerrado(2027, 1));
    }
}
