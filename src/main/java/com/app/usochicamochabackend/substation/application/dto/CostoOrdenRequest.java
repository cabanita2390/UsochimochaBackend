package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CostoOrdenRequest(
        @Schema(description = "MANO_OBRA o MATERIAL", example = "MATERIAL") String tipo,
        @Schema(example = "Mortero epóxico 5 kg") String descripcion,
        @Schema(description = "Contratista (mano de obra) o proveedor (material)", example = "Ferretería El Puente") String proveedor,
        @Schema(example = "2") BigDecimal cantidad,
        @Schema(description = "Valor total en pesos", example = "180000") BigDecimal valorTotal
) {
}
