package com.app.usochicamochabackend.moto.application.dto;

import java.time.LocalDate;

/**
 * tipoDocumento: "SOAT", "REVISION_TECNO", "LICENCIA"
 */
public record DocumentoExistenteResponse(
        Integer id,
        String tipoDocumento,
        LocalDate fechaVencimiento,
        String mesyear, // formato "YYYY-MM"
        String imagenUrl) {
}
