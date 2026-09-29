package com.app.usochicamochabackend.substation.application.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Única fuente de "hoy", "mes actual" y "mes cerrado" del módulo, siempre en hora de Colombia
 * y calculada en el servidor (nunca con la fecha del navegador ni del teléfono).
 * Mes cerrado = mes anterior al actual del año en curso; los años pasados están cerrados
 * completos y los futuros, abiertos completos.
 */
@Component
public class CalendarioMantenimiento {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final Clock reloj;

    public CalendarioMantenimiento() {
        this(Clock.system(ZONA));
    }

    /** Para tests: un reloj fijo. */
    public CalendarioMantenimiento(Clock reloj) {
        this.reloj = reloj;
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj.withZone(ZONA));
    }

    public int anioActual() {
        return hoy().getYear();
    }

    /** 1..12 */
    public int mesActual() {
        return hoy().getMonthValue();
    }

    public boolean mesCerrado(int anio, int mes) {
        int actual = anioActual();
        return anio < actual || (anio == actual && mes < mesActual());
    }
}
