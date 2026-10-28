package com.app.usochicamochabackend.substation.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginasTest {

    private static final Sort FECHA = Sort.by(Sort.Direction.DESC, "fecha");

    @Test
    void sinOrdenPedido_usaElDeFechaYDesempataPorId() {
        Pageable p = Paginas.estable(PageRequest.of(2, 20), FECHA);
        assertEquals(Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id")), p.getSort());
        assertEquals(2, p.getPageNumber());
        assertEquals(20, p.getPageSize());
    }

    @Test
    void conOrdenPedido_loRespetaYAgregaElId() {
        Pageable p = Paginas.estable(PageRequest.of(0, 20, Sort.by("resultado")), FECHA);
        assertEquals(Sort.by(Sort.Order.asc("resultado"), Sort.Order.desc("id")), p.getSort());
    }

    @Test
    void siYaOrdenaPorId_noLoRepite() {
        Sort pedido = Sort.by(Sort.Order.desc("fecha"), Sort.Order.asc("id"));
        assertEquals(pedido, Paginas.estable(PageRequest.of(0, 20, pedido), FECHA).getSort());
    }

    @Test
    void elTamanoTieneTopeDe500_loQuePideElMovil() {
        assertEquals(500, Paginas.estable(PageRequest.of(0, 100000), FECHA).getPageSize());
        assertEquals(500, Paginas.estable(PageRequest.of(0, 500), FECHA).getPageSize());
        assertEquals(500, Paginas.estable(Pageable.unpaged(), FECHA).getPageSize());
    }
}
