package com.app.usochicamochabackend.tire.application.dto;

import java.math.BigDecimal;

public record PosicionLlantaResponse(
        String codigo,
        String etiqueta,
        Integer orden,
        BigDecimal psiMin,
        BigDecimal psiObjetivo,
        BigDecimal psiMax) {
}
