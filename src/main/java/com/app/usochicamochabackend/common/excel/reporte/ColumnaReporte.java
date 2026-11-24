package com.app.usochicamochabackend.common.excel.reporte;

/** Una columna que el usuario puede quitar del reporte, y si sale marcada por defecto. */
public record ColumnaReporte(String clave, String titulo, boolean porDefecto) {
}
