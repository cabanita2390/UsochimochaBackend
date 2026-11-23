package com.app.usochicamochabackend.common.excel;

import java.util.function.Function;

/**
 * Una columna de una tabla de Excel: título, cómo se formatea y de dónde sale su valor.
 * {@code estado} (opcional) da el color de la celda: {@link Estado#OK}, {@link Estado#ALERTA},
 * {@link Estado#MAL} o null (sin color), los mismos tres colores de la web. {@code total}: la
 * columna se suma en la fila de totales al final de la tabla.
 */
public record ColumnaExcel<T>(String titulo, Tipo tipo, Function<T, Object> valor, Function<T, Estado> estado,
        boolean total) {

    public enum Tipo { TEXTO, NUMERO, DECIMAL, FECHA, FECHA_HORA, PORCENTAJE, MONEDA }

    public enum Estado { OK, ALERTA, MAL }

    public static <T> ColumnaExcel<T> texto(String titulo, Function<T, Object> valor) {
        return new ColumnaExcel<>(titulo, Tipo.TEXTO, valor, null, false);
    }

    /** Número entero (cantidades, horas). */
    public static <T> ColumnaExcel<T> numero(String titulo, Function<T, Object> valor) {
        return new ColumnaExcel<>(titulo, Tipo.NUMERO, valor, null, false);
    }

    /** Número con dos decimales (galones, rendimientos). */
    public static <T> ColumnaExcel<T> decimal(String titulo, Function<T, Object> valor) {
        return new ColumnaExcel<>(titulo, Tipo.DECIMAL, valor, null, false);
    }

    public static <T> ColumnaExcel<T> moneda(String titulo, Function<T, Object> valor) {
        return new ColumnaExcel<>(titulo, Tipo.MONEDA, valor, null, false);
    }

    public static <T> ColumnaExcel<T> fecha(String titulo, Function<T, Object> valor) {
        return new ColumnaExcel<>(titulo, Tipo.FECHA, valor, null, false);
    }

    public static <T> ColumnaExcel<T> fechaHora(String titulo, Function<T, Object> valor) {
        return new ColumnaExcel<>(titulo, Tipo.FECHA_HORA, valor, null, false);
    }

    /** El valor va de 0 a 100 (como lo muestra la web); en Excel queda con formato de porcentaje. */
    public static <T> ColumnaExcel<T> porcentaje(String titulo, Function<T, Object> valor) {
        return new ColumnaExcel<>(titulo, Tipo.PORCENTAJE, valor, null, false);
    }

    public ColumnaExcel<T> conEstado(Function<T, Estado> estado) {
        return new ColumnaExcel<>(titulo, tipo, valor, estado, total);
    }

    /** Se suma en la fila de totales. */
    public ColumnaExcel<T> conTotal() {
        return new ColumnaExcel<>(titulo, tipo, valor, estado, true);
    }
}
