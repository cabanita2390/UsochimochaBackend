package com.app.usochicamochabackend.common.excel;

import com.app.usochicamochabackend.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Periodo de una descarga (desde / hasta, ambos incluidos). Sin fechas es "todo el historial", para
 * que las llamadas anteriores sigan trayendo lo mismo.
 */
public record Periodo(LocalDate desde, LocalDate hasta) {

    /** Abreviaturas fijas: el formato del JDK cambia entre versiones ("ene" / "ene."). */
    private static final String[] MESES = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"};

    private static String fmt(LocalDate d) {
        return d.getDayOfMonth() + " " + MESES[d.getMonthValue() - 1] + " " + d.getYear();
    }

    public Periodo {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new BadRequestException("El periodo termina antes de empezar: " + desde + " a " + hasta);
        }
    }

    public static Periodo todo() {
        return new Periodo(null, null);
    }

    public boolean esTodo() {
        return desde == null && hasta == null;
    }

    public boolean contiene(LocalDate fecha) {
        if (esTodo()) {
            return true;
        }
        if (fecha == null) {
            return false;
        }
        return (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
    }

    public boolean contiene(LocalDateTime fechaHora) {
        return esTodo() || (fechaHora != null && contiene(fechaHora.toLocalDate()));
    }

    /** "1 ene 2026 – 31 dic 2026", "desde 1 ene 2026", "hasta …" o "Todo el historial". */
    public String texto() {
        if (esTodo()) {
            return "Todo el historial";
        }
        if (desde == null) {
            return "Hasta " + fmt(hasta);
        }
        if (hasta == null) {
            return "Desde " + fmt(desde);
        }
        return fmt(desde) + " – " + fmt(hasta);
    }

    /** Sufijo para el nombre del archivo: "_2026-01-01_a_2026-12-31" o "" si es todo. */
    public String sufijoArchivo() {
        if (esTodo()) {
            return "";
        }
        return "_" + (desde != null ? desde : "inicio") + "_a_" + (hasta != null ? hasta : "hoy");
    }
}
