package com.app.usochicamochabackend.moto.application.dto;

import java.time.LocalDate;

/**
 * tipoDocumento: "SOAT", "REVISION_TECNO", "LICENCIA"
 * estadoCheck: "Vigente", "Vencido", "Próximo a vencer" — leído de
 * insp_detalle_documentos
 */
public record DocumentoExistenteResponse(
        Integer id,
        String tipoDocumento,
        LocalDate fechaVencimiento,
        String mesyear, // formato "YYYY-MM"
        String imagenUrl,
        Integer vehiculoKilometrajeActual,
        String estadoCheck) {
}
