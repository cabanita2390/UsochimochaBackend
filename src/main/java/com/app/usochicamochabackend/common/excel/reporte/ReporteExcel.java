package com.app.usochicamochabackend.common.excel.reporte;

import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Un reporte a Excel del sistema. Solo dice QUÉ trae: título, si usa periodo, sus filtros, sus
 * columnas elegibles, quién lo puede descargar y cómo llena el libro. El CÓMO (portada, estilo,
 * nombre del archivo, permisos, respuesta HTTP) lo pone siempre el mismo motor
 * ({@link MotorReportes}), para que ninguna descarga tenga una forma propia.
 *
 * <p>Para agregar un reporte: un bean que implemente esta interfaz (o {@link Reporte#builder}).
 * Aparece solo en {@code GET /api/v1/reportes} y se descarga en {@code GET /api/v1/reportes/{clave}}.
 */
public interface ReporteExcel {

    String FLOTA = "Flota";
    String COMBUSTIBLE = "Combustible";
    String ESTACIONES = "Estaciones de Bombeo";

    Set<String> ROLES_WEB = Set.of("SUPERVISOR_OPERATIVO", "ADMIN");
    Set<String> ROLES_TODOS = Set.of("OPERARIO", "SUPERVISOR_OPERATIVO", "ADMIN");

    /** Identificador estable en la URL ("ordenes-maquinaria"). */
    String clave();

    /** Nombre del reporte ("Órdenes de trabajo · Maquinaria"). */
    String titulo();

    /** Módulo al que pertenece (agrupa el catálogo). */
    String modulo();

    /** Si pide periodo (desde / hasta). Los consolidados y las hojas de vida no. */
    default boolean usaPeriodo() {
        return false;
    }

    /** Atajo con el que abre el panel de periodo ("anio", "mes", "todo"…). */
    default String periodoInicial() {
        return "anio";
    }

    /** Filtros que el usuario elige en el panel (disciplina, estación…) o que la pantalla fija (tipo, activo). */
    default List<FiltroReporte> filtros() {
        return List.of();
    }

    /** Columnas que el usuario puede quitar; vacío = columnas fijas. */
    default List<ColumnaReporte> columnas() {
        return List.of();
    }

    /** Quién lo puede descargar. */
    default Set<String> roles() {
        return ROLES_WEB;
    }

    /** Periodo que sale en la portada; por defecto el pedido (Estaciones lo calcula del año y mes). */
    default Periodo periodo(PeticionReporte p) {
        return p.periodo();
    }

    /** Título del libro; por defecto el del reporte. */
    default String tituloLibro(PeticionReporte p) {
        return titulo();
    }

    /**
     * Nombre del archivo sin extensión: la clave con guiones bajos, más el periodo
     * ("ordenes_maquinaria_2026-01-01_a_2026-12-31") o la fecha de corte si no usa periodo.
     */
    default String nombreArchivo(PeticionReporte p) {
        return clave().replace('-', '_');
    }

    /** Llena el libro: filtros y cifras de la portada, y sus hojas. */
    void llenar(LibroExcel libro, PeticionReporte p) throws IOException;
}
