package com.app.usochicamochabackend.tire.application.dto;

import java.math.BigDecimal;

public record LecturaPresionLlantaResponse(
        String codigoPosicion,
        String etiquetaPosicion,
        Integer orden,
        BigDecimal psiMedido,
        BigDecimal psiMin,
        BigDecimal psiMax,
        /** OK | BAJA | ALTA | SIN_RANGO */
        String resultado) {
}
