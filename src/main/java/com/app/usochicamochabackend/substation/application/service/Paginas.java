package com.app.usochicamochabackend.substation.application.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Paginación estable de los listados del módulo (Ejecuciones, Órdenes).
 * <ul>
 *   <li>Siempre termina en {@code id}: al ordenar por una columna con valores repetidos (estación,
 *       resultado, prioridad…) Postgres no garantiza el orden entre empates, y al pasar de página
 *       unos registros salían repetidos y otros nunca aparecían (en la prueba de punta a punta, ordenar
 *       por Resultado mostraba 786 de 1117 visitas).</li>
 *   <li>Sin orden pedido usa el de por defecto (el móvil pide sin {@code sort}).</li>
 *   <li>Tamaño de página con tope: {@code size=100000} armaba toda la tabla en una sola respuesta.</li>
 * </ul>
 */
final class Paginas {

    /** El móvil baja las visitas no programadas del año de a 500: un tope menor le cortaría la descarga. */
    static final int TAMANO_MAXIMO = 500;

    private Paginas() {
    }

    static Pageable estable(Pageable pedido, Sort porDefecto) {
        Sort orden = pedido.getSort().isSorted() ? pedido.getSort() : porDefecto;
        if (orden.getOrderFor("id") == null) {
            orden = orden.and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if (pedido.isUnpaged()) {
            return PageRequest.of(0, TAMANO_MAXIMO, orden);
        }
        return PageRequest.of(pedido.getPageNumber(), Math.min(pedido.getPageSize(), TAMANO_MAXIMO), orden);
    }
}
